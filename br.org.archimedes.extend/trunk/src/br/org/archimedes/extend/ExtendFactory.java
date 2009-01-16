package br.org.archimedes.extend;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.org.archimedes.Constant;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.InvalidParameterException;
import br.org.archimedes.exceptions.NoActiveDrawingException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.factories.CommandFactory;
import br.org.archimedes.interfaces.Command;
import br.org.archimedes.interfaces.IntersectionManager;
import br.org.archimedes.interfaces.Parser;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.Selection;
import br.org.archimedes.parser.ReturnDecoratorParser;
import br.org.archimedes.parser.SelectionParser;
import br.org.archimedes.parser.SimpleSelectionParser;
import br.org.archimedes.parser.StringDecoratorParser;
import br.org.archimedes.polyline.Polyline;
import br.org.archimedes.rcp.extensionpoints.IntersectionManagerEPLoader;
import br.org.archimedes.undo.UndoCommand;

public class ExtendFactory implements CommandFactory {

    private boolean gotRef;

    private Collection<Element> references;

    private boolean active;

    private Command command;

    private int count;

    private ArrayList<Point> points;

    private IntersectionManager intersectionManager;


    public ExtendFactory () {

        intersectionManager = new IntersectionManagerEPLoader()
                .getIntersectionManager();
        deactivate();
    }

    public String begin () {

        active = true;
        gotRef = false;

        references = new ArrayList<Element>();

        command = null;
        count = 0;

        String returnValue = Messages.SelectRefs;
        try {
            Set<Element> selection = br.org.archimedes.Utils.getController()
                    .getCurrentSelectedElements();

            if (selection != null && !selection.isEmpty()) {
                returnValue = next(selection);
            }
        }
        catch (NoActiveDrawingException e) {
            returnValue = cancel();
        }
        catch (InvalidParameterException e) {
            // Should not happen
            e.printStackTrace();
        }

        return returnValue;
    }

    public String next (Object parameter) throws InvalidParameterException {

        String result = null;

        if ( !isDone()) {
            if (parameter == null) {
                active = false;
                command = null;
                result = Messages.Extended;
            }
            else if (parameter.equals("u") || parameter.equals("U")) {
                result = makeUndo();
            }
            else if ( !gotRef) {
                result = tryGetReference(parameter);
            }
            else {
                result = tryGetSelection(parameter);
                count++; // TODO Disconsider this when the command fails.
            }
        }

        return result;
    }

    /**
     * Makes an undo command.
     */
    private String makeUndo () {

        String returnMessage = br.org.archimedes.undo.Messages.UndoPerformed
                + Constant.NEW_LINE;

        command = null;
        if (count > 0) {
            command = new UndoCommand();
            count--;
            returnMessage += Messages.ExtendSelectElements;
        }
        else if (gotRef) {
            references = null;
            gotRef = false;
            returnMessage += Messages.SelectRefs;
        }
        else {
            returnMessage = br.org.archimedes.undo.Messages.notPerformed;
        }

        return returnMessage;
    }

    /**
     * Tries to get the reference elements from the parameter.
     * 
     * @param parameter
     *            The potential reference elements.
     * @return A message to the user.
     * @throws InvalidParameterException
     *             In case the parameter was not the reference elements.
     */
    @SuppressWarnings("unchecked")
    private String tryGetReference (Object parameter)
            throws InvalidParameterException {

        Set<Element> collection = null;
        try {
            collection = (Set<Element>) parameter;
        }
        catch (ClassCastException e) {
            throw new InvalidParameterException(Messages
                    .SelectRefs);
        }

        references = new HashSet<Element>();
        if (collection != null) {
            references.addAll(collection);
        }

        gotRef = true;
        return Messages.ExtendSelectElements;
    }

    /**
     * Tries to get elements to be cut from the parameter.
     * 
     * @param parameter
     *            The potential elements to be cut
     * @return A message to the user.
     * @throws InvalidParameterException
     *             In case the parameter was not the elements to be cut.
     */
    private String tryGetSelection (Object parameter)
            throws InvalidParameterException {

        String result = null;
        try {
            if (parameter == null) {
                throw new InvalidParameterException(Messages
                        .ExtendSelectElements);
            }
            Selection selection = (Selection) parameter;
            calculatePoints(selection);
            command = new ExtendCommand(references, points);
            result = Messages.ExtendSelectElements;
        }
        catch (ClassCastException e) {
            throw new InvalidParameterException(Messages.ExtendSelectElements);
        }
        catch (NullArgumentException e) {
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Calculates the list of points on which to perform the extend.
     * 
     * @param selection
     *            The selection to use.
     * @throws InvalidArgumentException
     * @throws NullArgumentException
     */
    private void calculatePoints (Selection selection)
            throws NullArgumentException, InvalidArgumentException {

        points = new ArrayList<Point>();

        Rectangle area = selection.getRectangle();

        if (area != null) {
            List<Point> borderPoints = new ArrayList<Point>();
            borderPoints.add(area.getUpperLeft());
            borderPoints.add(area.getUpperRight());
            borderPoints.add(area.getLowerRight());
            borderPoints.add(area.getLowerLeft());
            Polyline areaPl = new Polyline(borderPoints);

            Set<Element> elements = selection.getSelectedElements();
            for (Element element : elements) {
                Collection<Point> intersections = new ArrayList<Point>();
                try {
                    intersections = intersectionManager
                            .getIntersectionsBetween(element, areaPl);
                    for (Point intersection : intersections) {
                        if (element.contains(intersection)
                                && areaPl.contains(intersection)) {
                            points.add(intersection);
                        }
                    }
                }
                catch (NullArgumentException e) {
                    // Should not happen
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isDone () {

        return !active;
    }

    public String cancel () {

        String returnMsg = null;

        if ( !isDone()) {
            returnMsg = Messages.ExtendCancel;
        }

        deactivate();
        return returnMsg;
    }

    /**
     * Deactivates this factory.
     */
    private void deactivate () {

        active = false;
        references = null;
        gotRef = false;
        count = 0;

    }

    public void drawVisualHelper (Writer writer) {

        // Nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.commands.Command#getNextParser()
     */
    public Parser getNextParser () {

        Parser parser = null;
        if (active) {
            if ( !gotRef) {
                parser = new SimpleSelectionParser();
            }
            else {
                Parser selectionParser = new SelectionParser();
                Parser decoratedParser = new ReturnDecoratorParser(
                        selectionParser);
                parser = new StringDecoratorParser(decoratedParser, "u");
            }
        }
        return parser;
    }

    public List<Command> getCommands () {

        List<Command> cmds = new ArrayList<Command>();

        if (command != null) {
            cmds.add(command);
            command = null;
        }
        else {
            cmds = null;
        }
        return cmds;
    }

    public void drawVisualHelper () {

    }

    public String getName () {

        return "extend"; //$NON-NLS-1$
    }
}