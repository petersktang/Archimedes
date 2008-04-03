/*
 * Created on 13/04/2006
 */

package br.org.archimedes.offset;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import br.org.archimedes.Constant;
import br.org.archimedes.Utils;
import br.org.archimedes.controller.Controller;
import br.org.archimedes.controller.commands.PutOrRemoveElementCommand;
import br.org.archimedes.exceptions.InvalidParameterException;
import br.org.archimedes.exceptions.NoActiveDrawingException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.factories.CommandFactory;
import br.org.archimedes.gui.model.Workspace;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.interfaces.Command;
import br.org.archimedes.interfaces.Parser;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Offsetable;
import br.org.archimedes.model.Point;
import br.org.archimedes.parser.DistanceParser;
import br.org.archimedes.parser.PointParser;
import br.org.archimedes.parser.ReturnDecoratorParser;
import br.org.archimedes.parser.SimpleSelectionParser;
import br.org.archimedes.parser.StringDecoratorParser;
import br.org.archimedes.undo.UndoCommand;

/**
 * Belongs to package com.tarantulus.archimedes.commands.
 * 
 * @author night
 */
public class OffsetFactory implements CommandFactory {

    private Workspace workspace;

    private boolean active;

    private Double distance;

    private Double previousDistance;

    private Set<Offsetable> selection;

    private HashMap<Offsetable, Boolean> direction;

    private HashMap<Offsetable, Integer> numPositive, numNegative;

    private Command command;

    private int offsetCount;

    private DecimalFormat formatter;


    /**
     * Constructor of this Command.
     */
    public OffsetFactory () {

        workspace = Workspace.getInstance();
        previousDistance = 100.0;
        deactivate();
        formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.interpreter.Command#begin()
     */
    public String begin () {

        active = true;
        workspace.setMouseGrip(true);

        Controller controller = Controller.getInstance();
        String returnValue = Messages.getString("OffsetFactory.WaitDistance") + "(" //$NON-NLS-1$ //$NON-NLS-2$
                + formatter.format(previousDistance) + ")"; //$NON-NLS-1$
        try {
            Set<Element> selection = controller.getCurrentSelectedElements();
            if (selection != null && !selection.isEmpty()) {
                tryGetOffsetableSelection(selection);
            }
        }
        catch (NoActiveDrawingException e) {
            returnValue = cancel();
        }
        catch (InvalidParameterException e) {
            controller.deselectAll();
        }

        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.interpreter.Command#next(java.lang.String)
     */
    public String next (Object parameter) throws InvalidParameterException {

        String returnValue = null;

        if (isDone()) {
            throw new InvalidParameterException();
        }
        else if (parameter == null && distance != null && selection != null) {
            deactivate();
            returnValue = Messages.getString("OffsetFactory.Completed"); //$NON-NLS-1$
        }
        else if ("u".equals(parameter)) { //$NON-NLS-1$
            returnValue = makeUndo();
        }
        else if (distance == null) {
            returnValue = tryGetDistance(parameter);
        }
        else if (parameter != null && selection == null) {
            returnValue = tryGetOffsetableSelection(parameter);
        }
        else if (direction == null) {
            returnValue = tryGetDirection(parameter);
            try {
                offset();
            }
            catch (InvalidParameterException e) {
                returnValue = Messages
                        .getString("OffsetFactory.InvalidDistance"); //$NON-NLS-1$
            }
            direction = null;
        }
        else {
            throw new InvalidParameterException();
        }

        return returnValue;
    }

    /**
     * Tries to get a direction from the parameter.
     * 
     * @param parameter
     *            The parameter
     * @return A nice message to the user
     * @throws InvalidParameterException
     *             In case the parameter is not a direction
     */
    private String tryGetDirection (Object parameter)
            throws InvalidParameterException {

        Point point = null;
        Boolean dir = null;
        if ("+".equals(parameter) || "-".equals(parameter)) { //$NON-NLS-1$ //$NON-NLS-2$
            dir = "+".equals(parameter); //$NON-NLS-1$
        }
        else if (Boolean.class.equals(parameter.getClass())) {
            dir = (Boolean) parameter;
        }
        else {
            try {
                point = (Point) parameter;
            }
            catch (ClassCastException e) {
                throw new InvalidParameterException();
            }
        }

        direction = new HashMap<Offsetable, Boolean>();
        if (point != null) {
            for (Offsetable element : selection) {
                boolean direct = false;
                try {
                    direct = element.isPositiveDirection(point);
                }
                catch (NullArgumentException e) {
                    // Should never happen (we just tested the condition)
                    e.printStackTrace();
                }
                direction.put(element, direct);
            }
        }
        else {
            for (Offsetable element : selection) {
                direction.put(element, dir);
            }
        }

        return Messages.getString("OffsetFactory.WaitDirection"); //$NON-NLS-1$
    }

    /**
     * Tries to get a selection with at least one offsetable element from the
     * parameter.
     * 
     * @param parameter
     *            The parameter
     * @return A nice message to the user
     * @throws InvalidParameterException
     *             In case the parameter is not a selection
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private String tryGetOffsetableSelection (Object parameter)
            throws InvalidParameterException {

        Set<Element> received = null;
        try {
            received = (Set<Element>) parameter;
        }
        catch (ClassCastException e) {
            throw new InvalidParameterException();
        }

        selection = new HashSet<Offsetable>();
        for (Element element : received) {
            if (Utils.isInterfaceOf(element, Offsetable.class)) {
                Offsetable offsetableElement = (Offsetable) element;
                numPositive.put(offsetableElement, 0);
                numNegative.put(offsetableElement, 0);
                selection.add(offsetableElement);
            }
        }

        String result;
        if (selection.isEmpty()) {
            selection = null;
            result = Messages.getString("OffsetFactory.WaitSelection"); //$NON-NLS-1$
        }
        else {
            result = Messages.getString("OffsetFactory.WaitDirection"); //$NON-NLS-1$
        }

        return result;
    }

    /**
     * Tries to get a distance from the parameter
     * 
     * @param parameter
     *            The parameter
     * @return A nice message to the user
     * @throws InvalidParameterException
     *             In case the parameter is not a distance
     */
    private String tryGetDistance (Object parameter)
            throws InvalidParameterException {

        String result = null;

        try {
            distance = (Double) parameter;
        }
        catch (ClassCastException e) {
            throw new InvalidParameterException();
        }

        if (distance == null) {
            distance = previousDistance;
        }
        if (selection == null) {
            result = Messages.getString("OffsetFactory.WaitSelection"); //$NON-NLS-1$
        }
        else {
            result = Messages.getString("OffsetFactory.WaitDirection"); //$NON-NLS-1$
        }

        return result;
    }

    /**
     * Creates the commands unless some element cannot be offseted with the
     * specified distance.
     * 
     * @throws InvalidParameterException
     *             Thrown if the distance is too big for some element.
     */
    private void offset () throws InvalidParameterException {

        List<Element> offseteds = createOffsetedElements();

        try {
            if (offseteds.size() > 0) {
                command = new PutOrRemoveElementCommand(offseteds, false);
                offsetCount++;
            }
        }
        catch (NullArgumentException e) {
            // Should not throw this exception
            e.printStackTrace();
        }
    }

    /**
     * Creates the list of offseted elements unless some element cannot be
     * offseted with the specified distance.
     * 
     * @throws InvalidParameterException
     *             Thrown if the distance is too big for some element.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private List<Element> createOffsetedElements ()
            throws InvalidParameterException {

        double localDistance;
        boolean localPositive;
        List<Element> offseteds = new ArrayList<Element>();
        // TODO Pensar em algum jeito melhor de fazer isso
        Map<Offsetable, Integer> localNumPositive, localNumNegative;
        localNumPositive = (Map<Offsetable, Integer>) numPositive.clone();
        localNumNegative = (Map<Offsetable, Integer>) numNegative.clone();

        for (Offsetable element : selection) {
            localDistance = distance;
            localPositive = direction.get(element);

            if (localPositive) {
                numPositive.put(element, numPositive.get(element) + 1);
                localDistance = numPositive.get(element) * distance;
            }
            else {
                numNegative.put(element, numNegative.get(element) + 1);
                localDistance = numNegative.get(element) * distance;
            }

            localDistance *= (localPositive ? 1 : -1);

            Element offseted = null;
            try {
                offseted = element.cloneWithDistance(localDistance);
            }
            catch (InvalidParameterException e) {
                numPositive = (HashMap<Offsetable, Integer>) localNumPositive;
                numNegative = (HashMap<Offsetable, Integer>) localNumNegative;
                throw e;
            }
            offseteds.add(offseted);
        }
        return offseteds;
    }

    /**
     * Makes an undo command.
     */
    private String makeUndo () {

        ResourceBundle undoMessages = ResourceBundle
                .getBundle("com.tarantulus.archimedes.i18n.factory.UndoMessages"); //$NON-NLS-1$
        String returnMessage = undoMessages.getString(Messages
                .getString("OffsetFactory.UndoPerformed")) //$NON-NLS-1$
                + Constant.NEW_LINE;

        command = null;
        if (offsetCount > 0) {
            command = new UndoCommand();
            offsetCount--;
            returnMessage += Messages.getString("OffsetFactory.WaitSelection"); //$NON-NLS-1$
        }
        else if (selection != null) {
            selection = null;
            Controller.getInstance().deselectAll();
            numPositive = new HashMap<Offsetable, Integer>();
            numNegative = new HashMap<Offsetable, Integer>();
            returnMessage += Messages.getString("OffsetFactory.WaitSelection"); //$NON-NLS-1$
        }
        else if (distance != null) {
            distance = null;
            returnMessage += Messages.getString("OffsetFactory.WaitDistance") + "(" //$NON-NLS-1$ //$NON-NLS-2$
                    + formatter.format(previousDistance) + ")"; //$NON-NLS-1$
        }
        else {
            returnMessage = undoMessages.getString(Messages
                    .getString("OffsetFactory.UndoNotPerformed")); //$NON-NLS-1$
        }

        return returnMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.interpreter.Command#isDone()
     */
    public boolean isDone () {

        return !active;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.interpreter.Command#cancel()
     */
    public String cancel () {

        deactivate();
        return Messages.getString("OffsetFactory.cancel"); //$NON-NLS-1$
    }

    /**
     * Deactivates the command.
     */
    private void deactivate () {

        if (distance != null) {
            previousDistance = distance;
        }
        distance = null;
        selection = null;
        direction = null;

        numPositive = new HashMap<Offsetable, Integer>();
        numNegative = new HashMap<Offsetable, Integer>();
        offsetCount = 0;

        active = false;
        workspace.setMouseGrip(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.interpreter.Command#toString()
     */
    public String toString () {

        return "offset"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.commands.Command#getNextParser()
     */
    public Parser getNextParser () {

        Parser returnParser = null;
        if ( !active) {
            returnParser = null;
        }
        else if (distance == null) {
            returnParser = new DistanceParser();
        }
        else if (selection == null) {
            Parser decoratedParser = new SimpleSelectionParser();
            returnParser = new StringDecoratorParser(decoratedParser, "u"); //$NON-NLS-1$
        }
        else if (direction == null) {
            String[] patterns = new String[] {"+", "-", "u"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            returnParser = new StringDecoratorParser(new PointParser(),
                    patterns);
            returnParser = new ReturnDecoratorParser(returnParser);
        }

        return returnParser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.factories.CommandFactory#getCommands()
     */
    public List<Command> getCommands () {

        List<Command> commands = null;

        if (command != null) {
            commands = new ArrayList<Command>();
            commands.add(command);
            command = null;
        }

        return commands;
    }

    /**
     * @see br.org.archimedes.factories.CommandFactory#drawVisualHelper()
     */
    public void drawVisualHelper () {

        if (distance != null && selection != null && direction == null) {
            double localDistance;
            Point direction = workspace.getActualMousePosition();
            boolean localPositive = false;
            for (Offsetable element : selection) {
                localDistance = distance;
                try {
                    localPositive = element.isPositiveDirection(direction);
                }
                catch (NullArgumentException e) {
                    // Should never happen
                    e.printStackTrace();
                }

                if (localPositive) {
                    localDistance = (numPositive.get(element) + 1) * distance;
                }
                else {
                    localDistance = (numNegative.get(element) + 1) * distance;
                }

                localDistance *= (localPositive ? 1 : -1);
                Element copyElement;
                try {
                    copyElement = element.cloneWithDistance(localDistance);
                    copyElement.draw(OpenGLWrapper.getInstance());
                }
                catch (InvalidParameterException e) {
                    // Will often happen. Don't want to print it.
                }
            }
        }
    }

    /**
     * @see br.org.archimedes.factories.CommandFactory#getName()
     */
    public String getName () {

        return "offset"; //$NON-NLS-1$
    }
}