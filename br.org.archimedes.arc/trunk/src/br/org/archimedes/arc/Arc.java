/**
 * This file was created on 2007/03/12, 07:51:43, by nitao. It is part of
 * br.org.archimedes.arc on the br.org.archimedes.arc project.
 */

package br.org.archimedes.arc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import br.org.archimedes.Constant;
import br.org.archimedes.Geometrics;
import br.org.archimedes.curvedshape.CurvedShape;
import br.org.archimedes.exceptions.IllegalActionException;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.gui.opengl.OpenGLWrapper;
import br.org.archimedes.model.ComparablePoint;
import br.org.archimedes.model.DoubleKey;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Point;
import br.org.archimedes.model.Rectangle;
import br.org.archimedes.model.ReferencePoint;
import br.org.archimedes.model.Vector;
import br.org.archimedes.model.references.CirclePoint;
import br.org.archimedes.model.references.SquarePoint;
import br.org.archimedes.model.references.TrianglePoint;

/**
 * Belongs to package br.org.archimedes.arc.
 * 
 * @author nitao
 */
public class Arc extends CurvedShape {

    private Point initialPoint;

    private Point endingPoint;

    private Point intermediatePoint;


    /**
     * Constructor. Always build the arc in the counter clockwise order.
     * 
     * @param initialPoint
     *            The initial point
     * @param intermediatePoint
     *            A point that is contained in the arc and is different from the
     *            initial and ending point
     * @param endingPoint
     *            The ending point
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    public Arc (Point initialPoint, Point intermediatePoint, Point endingPoint)
            throws NullArgumentException, InvalidArgumentException {

        if (initialPoint == null || intermediatePoint == null
                || endingPoint == null) {
            throw new NullArgumentException();
        }

        createInternalRepresentation(initialPoint.clone(), intermediatePoint
                .clone(), endingPoint.clone());
    }

    /**
     * Constructor.
     * 
     * @param initialPoint
     *            The initial point
     * @param endingPoint
     *            The ending point
     * @param centerPoint
     *            The center point
     * @param counterclock
     *            true if the points are in counter clock order, false
     *            otherwise.
     * @throws NullArgumentException
     *             Thrown if any argument is null
     * @throws InvalidArgumentException
     *             Throw if the arc is invalid.
     */
    public Arc (Point initialPoint, Point endingPoint, Point centerPoint,
            boolean counterclock) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || endingPoint == null || centerPoint == null) {
            throw new NullArgumentException();
        }
        if (initialPoint.equals(endingPoint)
                || initialPoint.equals(centerPoint)
                || endingPoint.equals(centerPoint)) {
            throw new InvalidArgumentException();
        }

        if (counterclock) {
            this.initialPoint = initialPoint.clone();
            this.endingPoint = endingPoint.clone();
        }
        else {
            this.initialPoint = endingPoint.clone();
            this.endingPoint = initialPoint.clone();
        }
        this.centerPoint = centerPoint.clone();
        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, centerPoint);
    }

    /**
     * Adjusts the arc points so that they are in counter clockwise order.
     * 
     * @param initialPoint
     *            The initial point
     * @param intermediatePoint
     *            A point that is contained in the arc and is different from the
     *            initial and ending point
     * @param endingPoint
     *            The ending point
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    private void createInternalRepresentation (Point initialPoint,
            Point intermediatePoint, Point endingPoint)
            throws InvalidArgumentException, NullArgumentException {

        this.initialPoint = initialPoint;
        this.endingPoint = endingPoint;
        this.centerPoint = Geometrics.getCircumcenter(initialPoint,
                intermediatePoint, endingPoint);

        double initialAngle = Geometrics.calculateAngle(centerPoint,
                initialPoint);
        double middleAngle = Geometrics.calculateAngle(centerPoint,
                intermediatePoint);
        double endingAngle = Geometrics
                .calculateAngle(centerPoint, endingPoint);

        boolean isClockwise = (endingAngle > initialAngle && (middleAngle < initialAngle || middleAngle > endingAngle));
        isClockwise = isClockwise
                || (initialAngle > endingAngle && (endingAngle < middleAngle && middleAngle < initialAngle));

        if (isClockwise) {
            Point tempPoint = this.initialPoint;
            this.initialPoint = this.endingPoint;
            this.endingPoint = tempPoint;
        }
        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, this.centerPoint);
    }

    /**
     * Constructor.
     * 
     * @param initialPoint
     *            The initial point
     * @param endingPoint
     *            The ending point
     * @param centerPoint
     *            The center point
     * @param direction
     *            The direction to create the arc
     * @throws NullArgumentException
     *             Thrown if any of the points is null
     * @throws InvalidArgumentException
     *             Thrown if the points are collinear
     */
    public Arc (Point initialPoint, Point endingPoint, Point centerPoint,
            Point direction) throws NullArgumentException,
            InvalidArgumentException {

        if (initialPoint == null || centerPoint == null || endingPoint == null
                || direction == null) {
            throw new NullArgumentException();
        }

        if (Math.abs(Geometrics.calculateDeterminant(initialPoint, centerPoint,
                endingPoint)) <= Constant.EPSILON) {
            double initialToCenter = Geometrics.calculateDistance(initialPoint,
                    centerPoint);
            double endingToCenter = Geometrics.calculateDistance(endingPoint,
                    centerPoint);

            if (Math.abs(initialToCenter - endingToCenter) > Constant.EPSILON) {
                throw new InvalidArgumentException();
            }
        }

        this.centerPoint = centerPoint.clone();

        double arcAngle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, endingPoint);
        double dirAngle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, direction);

        boolean isClockwise = (dirAngle > arcAngle);

        this.initialPoint = initialPoint.clone();
        this.endingPoint = endingPoint.clone();

        if (isClockwise) {
            Point tempPoint = this.initialPoint;
            this.initialPoint = this.endingPoint;
            this.endingPoint = tempPoint;
        }

        this.intermediatePoint = calculateMidPoint(this.initialPoint,
                this.endingPoint, this.centerPoint);
    }

    public Arc clone () {

        Arc arc = null;

        try {
            arc = new Arc(initialPoint.clone(), intermediatePoint.clone(),
                    endingPoint.clone());
            arc.setLayer(parentLayer);
        }
        catch (NullArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }

        return arc;
    }

    public boolean equals (Object object) {

        boolean result = false;

        if (object != null) {
            try {
                Arc arc = (Arc) object;
                result = this.equals(arc);
            }
            catch (ClassCastException e) {
                // The elements are not equal
            }
        }

        return result;
    }

    public boolean equals (Arc arc) {

        boolean result = true;

        if (arc == null) {
            result = false;
        }
        else if ( !this.centerPoint.equals(arc.centerPoint)) {
            result = false;
        }
        else if ( !this.initialPoint.equals(arc.initialPoint)
                && !this.initialPoint.equals(arc.endingPoint)) {
            result = false;
        }
        else if ( !this.endingPoint.equals(arc.initialPoint)
                && !this.endingPoint.equals(arc.endingPoint)) {
            result = false;
        }
        else {
            try {
                double intermediatePointSign1 = Geometrics
                        .calculateDeterminant(initialPoint, endingPoint,
                                intermediatePoint);
                double intermediatePointSign2 = Geometrics
                        .calculateDeterminant(initialPoint, endingPoint,
                                arc.intermediatePoint);

                if ((intermediatePointSign1 * intermediatePointSign2) < 0.0) {
                    result = false;
                }
            }
            catch (NullArgumentException e) {
                // Should never happen
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * @return The radius of the arc
     */
    public double getRadius () {

        double radius = 0.0;

        try {
            radius = Geometrics.calculateDistance(centerPoint, initialPoint);
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }

        return radius;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getIntersection(com.tarantulus.archimedes.model.Element)
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public Collection<Point> getIntersection (Element element)
            throws NullArgumentException {

        if (element == null) {
            throw new NullArgumentException();
        }

        // TODO Implementar a interseccao
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getBoundaryRectangle()
     */
    public Rectangle getBoundaryRectangle () {

        double maxX = Math.max(initialPoint.getX(), endingPoint.getX());
        double minX = Math.min(initialPoint.getX(), endingPoint.getX());
        double maxY = Math.max(initialPoint.getY(), endingPoint.getY());
        double minY = Math.min(initialPoint.getY(), endingPoint.getY());

        double rightSide = centerPoint.getX() + getRadius();
        if ( !contains(rightSide, centerPoint.getY())) {
            rightSide = maxX;
        }

        double leftSide = centerPoint.getX() - getRadius();
        if ( !contains(leftSide, centerPoint.getY())) {
            leftSide = minX;
        }

        double topSide = centerPoint.getY() + getRadius();
        if ( !contains(centerPoint.getX(), topSide)) {
            topSide = maxY;
        }

        double bottomSide = centerPoint.getY() - getRadius();
        if ( !contains(centerPoint.getX(), bottomSide)) {
            bottomSide = minY;
        }

        return new Rectangle(leftSide, topSide, rightSide, bottomSide);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getReferencePoints(com.tarantulus.archimedes.model.Rectangle)
     */
    public Collection<ReferencePoint> getReferencePoints (Rectangle area) {

        Collection<ReferencePoint> references = new ArrayList<ReferencePoint>();
        List<Point> allPoints = getPoints();
        if (area != null) {
            if (initialPoint.isInside(area)) {
                try {
                    references.add(new SquarePoint(initialPoint, initialPoint));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
            if (endingPoint.isInside(area)) {
                try {
                    references.add(new SquarePoint(endingPoint, endingPoint));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
            if (centerPoint.isInside(area)) {
                try {
                    references.add(new CirclePoint(centerPoint, allPoints));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
            if (intermediatePoint.isInside(area)) {
                try {
                    references.add(new TrianglePoint(intermediatePoint,
                            intermediatePoint));
                }
                catch (NullArgumentException e) {
                    // Should never reach this block
                    e.printStackTrace();
                }
            }
        }

        return references;
    }

    /**
     * @param initialPoint
     *            The initial point of the arc
     * @param endingPoint
     *            The ending point of the arc
     * @param centerPoint
     *            The center point of the arc
     * @return The point corresponding to the mid point of this arc.
     */
    private Point calculateMidPoint (Point initialPoint, Point endingPoint,
            Point centerPoint) {

        Point midPoint;
        double angle = Geometrics.calculateRelativeAngle(centerPoint,
                initialPoint, endingPoint);
        angle /= 2;
        midPoint = initialPoint.clone();
        try {
            midPoint.rotate(centerPoint, angle);
        }
        catch (NullArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }
        return midPoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#getProjectionOf(com.tarantulus.archimedes.model.Point)
     */
    public Point getProjectionOf (Point point) throws NullArgumentException {

        if (point == null) {
            throw new NullArgumentException();
        }

        Point closer = null, farther = null;
        // TODO Implementar a projecao
        // try {
        // Line line = new Line(centerPoint, point);
        // Collection<Point> intersectionWithLine =
        // getIntersectionWithLine(line);
        // double closestDist = Double.MAX_VALUE;
        // for (Point intersection : intersectionWithLine) {
        // double dist = Geometrics.calculateDistance(point, intersection);
        // if (dist < closestDist) {
        // if (closer != null) {
        // farther = closer;
        // }
        // closer = intersection;
        // closestDist = dist;
        // }
        // else {
        // farther = intersection;
        // }
        // }
        // }
        // catch (InvalidArgumentException e) {
        // // May happen
        // e.printStackTrace();
        // }

        Point projection = null;
        if (contains(closer) || !contains(farther)) {
            projection = closer;
        }
        else {
            projection = farther;
        }

        return projection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#contains(com.tarantulus.archimedes.model.Point)
     */
    public boolean contains (Point point) throws NullArgumentException {

        boolean result = false;

        double distance = Geometrics.calculateDistance(point, centerPoint);
        double radius = Geometrics.calculateDistance(initialPoint, centerPoint);

        if (Math.abs(distance - radius) <= Constant.EPSILON) {
            double intermediateSign = Geometrics.calculateDeterminant(
                    initialPoint, endingPoint, intermediatePoint);
            double pointSign = Geometrics.calculateDeterminant(initialPoint,
                    endingPoint, point);

            result = ((intermediateSign * pointSign) >= 0.0);
        }

        return result;
    }

    /**
     * Returns true if the arc contains a point determined by x and y.
     * 
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     * @return true if the arc contains the point, false otherwise.
     */
    private boolean contains (double x, double y) {

        boolean result = false;
        try {
            result = contains(new Point(x, y));
        }
        catch (NullArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }
        return result;
    }

    public boolean isPositiveDirection (Point point) {

        boolean isOutside = false;

        try {
            if (Geometrics.calculateDistance(this.getCenter(), point) > Geometrics
                    .calculateDistance(this.getCenter(), this.getInitialPoint())) {
                isOutside = true;
            }
        }
        catch (NullArgumentException e) {
            // Should not reach this block
            e.printStackTrace();
        }
        return isOutside;
    }

    public Element cloneWithDistance (double distance)
            throws IllegalActionException {

        if (distance < 0) {
            if (Math.abs(getRadius() - distance) <= Constant.EPSILON
                    || Math.abs(distance) > getRadius()) {
                throw new IllegalActionException();
            }
        }

        Vector startVector = new Vector(centerPoint, initialPoint);
        startVector = Geometrics.normalize(startVector);
        startVector = startVector.multiply(getRadius() + distance);

        Point p1 = centerPoint.addVector(startVector);

        Vector midVector = new Vector(centerPoint, intermediatePoint);
        midVector = Geometrics.normalize(midVector);
        midVector = midVector.multiply(getRadius() + distance);

        Point p2 = centerPoint.addVector(midVector);

        Vector endVector = new Vector(centerPoint, endingPoint);
        endVector = Geometrics.normalize(endVector);
        endVector = endVector.multiply(getRadius() + distance);

        Point p3 = centerPoint.addVector(endVector);

        Arc arc = null;
        try {
            arc = new Arc(p1, p2, p3);
            arc.setLayer(parentLayer);
        }
        catch (NullArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should not reach this code
            e.printStackTrace();
        }

        return arc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Trimmable#trim(java.util.Collection,
     *      com.tarantulus.archimedes.model.Point)
     */
    public Collection<Element> trim (Collection<Element> references, Point click) {

        Collection<Element> trimResult = new ArrayList<Element>();
        Collection<Point> intersectionPoints = getIntersectionPoints(references);
        SortedSet<ComparablePoint> sortedPointSet = getSortedPointSet(
                initialPoint, intersectionPoints);

        ComparablePoint clickPoint = null;
        ComparablePoint initial = null;
        try {
            double key = getArcAngle(click);
            clickPoint = new ComparablePoint(click, new DoubleKey(key));
            initial = new ComparablePoint(initialPoint, new DoubleKey(0.0));
        }
        catch (NullArgumentException e) {
            // Should never reach
            e.printStackTrace();
        }

        // Consider only point within the arc
        sortedPointSet = sortedPointSet.tailSet(initial);

        SortedSet<ComparablePoint> negativeIntersections = sortedPointSet
                .headSet(clickPoint);
        SortedSet<ComparablePoint> positiveIntersections = sortedPointSet
                .tailSet(clickPoint);

        try {
            if (negativeIntersections.size() == 0
                    && positiveIntersections.size() > 0) {
                Point firstPositive = positiveIntersections.first().getPoint();
                Collection<Point> points = new ArrayList<Point>();
                points.add(firstPositive);
                points.add(endingPoint);
                Element arc = new Arc(firstPositive, endingPoint, centerPoint,
                        true);
                arc.setLayer(getLayer());

                trimResult.add(arc);
            }
            else if (positiveIntersections.size() == 0
                    && negativeIntersections.size() > 0) {
                Point lastNegative = negativeIntersections.last().getPoint();
                Collection<Point> points = new ArrayList<Point>();
                points.add(lastNegative);
                points.add(initialPoint);
                Element arc = new Arc(initialPoint, lastNegative, centerPoint,
                        true);
                arc.setLayer(getLayer());

                trimResult.add(arc);
            }
            else if (negativeIntersections.size() > 0
                    && positiveIntersections.size() > 0) {
                Point firstPositive = positiveIntersections.first().getPoint();
                Point lastNegative = negativeIntersections.last().getPoint();
                Collection<Point> points = new ArrayList<Point>();
                points.add(lastNegative);
                points.add(initialPoint);
                Element arc1 = new Arc(initialPoint, lastNegative, centerPoint,
                        true);
                arc1.setLayer(getLayer());

                trimResult.add(arc1);

                points = new ArrayList<Point>();
                points.add(firstPositive);
                points.add(endingPoint);
                Element arc2 = new Arc(firstPositive, endingPoint, centerPoint,
                        true);
                arc2.setLayer(getLayer());

                trimResult.add(arc2);
            }
        }
        catch (NullArgumentException e) {
            // Should not catch this exception
            e.printStackTrace();
        }
        catch (InvalidArgumentException e) {
            // Should not catch this exception
            e.printStackTrace();
        }

        return trimResult;

    }

    /**
     * Gets all the proper intersections of the collection of references with
     * this element. The initial point and the ending point are not considered
     * intersections.
     * 
     * @param references
     *            A collection of references
     * @return A collection of proper intersections points
     */
    private Collection<Point> getIntersectionPoints (
            Collection<Element> references) {

        Collection<Point> intersectionPoints = new ArrayList<Point>();

        for (Element element : references) {
            try {
                if (element != this) {
                    Collection<Point> inter = element.getIntersection(this);
                    for (Point point : inter) {
                        if (this.contains(point) && element.contains(point)
                                && !this.initialPoint.equals(point)
                                && !this.endingPoint.equals(point)) {
                            intersectionPoints.add(point);
                        }
                    }
                }
            }
            catch (NullArgumentException e) {
                // Should never catch this exception
                e.printStackTrace();
            }
        }

        return intersectionPoints;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.PointSortable#getSortedPointSet(com.tarantulus.archimedes.model.Point,
     *      java.util.Collection)
     */
    public SortedSet<ComparablePoint> getSortedPointSet (Point referencePoint,
            Collection<Point> points) {

        SortedSet<ComparablePoint> sortedSet = new TreeSet<ComparablePoint>();

        boolean invertOrder = referencePoint.equals(endingPoint);
        for (Point point : points) {
            try {
                double key = getArcAngle(point);
                if (Math.abs(key) > Constant.EPSILON && invertOrder) {
                    key = 1 / key;
                }
                ComparablePoint orderedPoint = new ComparablePoint(point,
                        new DoubleKey(key));
                sortedSet.add(orderedPoint);
            }
            catch (NullArgumentException e) {
                // Should not catch this exception
                e.printStackTrace();
            }

        }

        return sortedSet;
    }

    /**
     * @param point
     *            The point whose angle is to be calculated
     * @return The angle relative to the initial Point. The angle is between
     *         -2*PI and 2*PI.
     */
    private double getArcAngle (Point point) {

        double arc = 0;
        boolean contained = true;
        try {
            contained = contains(point);
        }
        catch (NullArgumentException e) {
            // Should not happen
            e.printStackTrace();
        }
        if (contained) {
            arc = Geometrics.calculateRelativeAngle(centerPoint, initialPoint,
                    point);
        }
        else {
            arc = Geometrics.calculateRelativeAngle(centerPoint, endingPoint,
                    point);
            arc -= Geometrics.calculateRelativeAngle(centerPoint, endingPoint,
                    initialPoint);
        }
        return arc;
    }

    public boolean isCollinearWith (Element element) {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.elements.Element#isParallelTo(com.tarantulus.archimedes.model.elements.Element)
     */
    public boolean isParallelTo (Element element) {

        return false;
    }

    /**
     * @see br.org.archimedes.model.Element#draw(br.org.archimedes.gui.opengl.OpenGLWrapper)
     */
    @Override
    public void draw (OpenGLWrapper wrapper) {

        Point center = this.getCenter();

        double initialAngle = 0.0;
        double endingAngle = 0.0;
        try {
            initialAngle = Geometrics.calculateAngle(center, this
                    .getInitialPoint());
            endingAngle = Geometrics.calculateAngle(center, this
                    .getEndingPoint());
        }
        catch (NullArgumentException e) {
            // Should never reach this block
            e.printStackTrace();
        }

        if (initialAngle > endingAngle) {
            endingAngle += 2.0 * Math.PI;
        }
        this.drawCurvedShape(wrapper, center, initialAngle, endingAngle);
    }

    /**
     * @return Returns the endingPoint.
     */
    public Point getEndingPoint () {

        return endingPoint;
    }

    /**
     * @return Returns the initialPoint.
     */
    public Point getInitialPoint () {

        return initialPoint;
    }

    /**
     * @return Returns the intermediatePoint.
     */
    public Point getIntermediatePoint () {

        return intermediatePoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.elements.Element#getPoints()
     */
    @Override
    public List<Point> getPoints () {

        List<Point> points = new ArrayList<Point>();
        points.add(initialPoint);
        points.add(endingPoint);
        points.add(intermediatePoint);
        points.add(centerPoint);
        return points;
    }

    public String toString () {

        return centerPoint.toString() + " with radius " + getRadius() //$NON-NLS-1$
                + " from " + initialPoint.toString() + " to " //$NON-NLS-1$ //$NON-NLS-2$
                + endingPoint.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Extendable#getNearestIntersection(java.util.Collection,
     *      com.tarantulus.archimedes.model.Point)
     */
    public Point getNearestIntersection (Collection<Element> references,
            Point toExtend) {

        Point nearestIntersection = null;

        Collection<Point> intersectionPoints = getIntersectionPoints(references);
        SortedSet<ComparablePoint> sortedPointSet = getSortedPointSet(toExtend,
                intersectionPoints);

        ComparablePoint extendPoint = null;
        try {
            extendPoint = new ComparablePoint(toExtend, new DoubleKey(0.0));
        }
        catch (NullArgumentException e) {
            // Should never reach
            e.printStackTrace();
        }

        SortedSet<ComparablePoint> negativeIntersections = sortedPointSet
                .headSet(extendPoint);

        if (negativeIntersections.size() > 0) {
            nearestIntersection = negativeIntersections.first().getPoint();
        }

        return nearestIntersection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.elements.Element#getNearestExtremePoint(com.tarantulus.archimedes.model.Point)
     */
    @Override
    public Point getNearestExtremePoint (Point point)
            throws NullArgumentException {

        double distanceToInitial = Geometrics.calculateDistance(point,
                getInitialPoint());
        double distanceToEnding = Geometrics.calculateDistance(point,
                getEndingPoint());

        Point returnPoint = null;
        if (distanceToEnding < distanceToInitial) {
            returnPoint = getEndingPoint();
        }
        else {
            returnPoint = getInitialPoint();
        }
        return returnPoint;
    }

    /**
     * @see br.org.archimedes.model.Element#intersects(br.org.archimedes.model.Rectangle)
     */
    @Override
    public boolean intersects (Rectangle rectangle)
            throws NullArgumentException {

        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.tarantulus.archimedes.model.Element#move(Collection<Point>,
     *      Vector)
     */
    public void move (Collection<Point> points, Vector vector)
            throws NullArgumentException {

        super.move(points, vector);
        try {
            createInternalRepresentation(initialPoint, intermediatePoint,
                    endingPoint);
        }
        catch (InvalidArgumentException e) {
            // If the arc is invalid, undoes the move
            super.move(points, vector.multiply( -1.0));
        }
        catch (NullArgumentException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    /**
     * Este m�todo foi reescrito pois, apesar de o mirror ter sido feito, o arco ainda guardava
     * as refer�ncias inalteradas para o ending point e initialPoint. Logo, para que o mirror 
     * funcionasse corretamente, havia a necessidade de trocar o initialPoint com o endingPoint.
     * 
     * @author Victor
     * @author Eduardo
     */
	@Override
	public void mirror(Point p1, Point p2) throws NullArgumentException, IllegalActionException {
		// TODO Auto-generated method stub
		super.mirror(p1, p2);
		
		Point auxiliarPoint = initialPoint;
		
		initialPoint = endingPoint;
        endingPoint = auxiliarPoint;		
	}    
}