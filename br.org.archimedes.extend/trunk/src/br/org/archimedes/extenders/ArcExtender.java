/**
 * Copyright (c) 2008, 2009 Hugo Corbucci and others.<br>
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html<br>
 * <br>
 * Contributors:<br>
 * Jonas K. Hirata - initial API and implementation<br>
 * Hugo Corbucci - later contributions<br>
 * <br>
 * This file was created on 2008/07/16, 23:59:46, by Jonas K. Hirata.<br>
 * It is part of package br.org.archimedes.extend.line on the br.org.archimedes.extend project.<br>
 */

package br.org.archimedes.extenders;

import java.util.Collection;

import br.org.archimedes.Geometrics;
import br.org.archimedes.arc.Arc;
import br.org.archimedes.circle.Circle;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.extend.interfaces.Extender;
import br.org.archimedes.interfaces.IntersectionManager;
import br.org.archimedes.model.Element;
import br.org.archimedes.model.Point;
import br.org.archimedes.rcp.extensionpoints.IntersectionManagerEPLoader;

public class ArcExtender implements Extender {

	public void extend(Element element, Collection<Element> references,
			Point click) throws NullArgumentException {

		IntersectionManager intersectionManager = new IntersectionManagerEPLoader()
				.getIntersectionManager();

		if (element == null || references == null || click == null) {
			throw new NullArgumentException();
		}

		Arc arc = (Arc) element;
		boolean initial = false;

		Point nearestExtremePoint = getNearestExtremePoint(arc, click);
		if (nearestExtremePoint == arc.getInitialPoint())
			initial = true;

		Circle circle;
		try {
			circle = new Circle(arc.getCenter(), arc.getRadius());
			Collection<Point> intersectionPoints = intersectionManager
					.getIntersectionsBetween(circle, references);

			if (intersectionPoints.size() != 0) {
				Point nearestReferencePoint = null;
				double minDistance = Double.MAX_VALUE;

				for (Point point : intersectionPoints) {
					double distanceToRef = Geometrics.calculateDistance(point,
							nearestExtremePoint);
					if (!arc.contains(point) && distanceToRef < minDistance) {
						nearestReferencePoint = point;
						minDistance = distanceToRef;
					}
				}
				
				if (initial) {
					arc.getInitialPoint().setX(nearestReferencePoint.getX());
					arc.getInitialPoint().setY(nearestReferencePoint.getY());
				} else {
					arc.getEndingPoint().setX(nearestReferencePoint.getX());
					arc.getEndingPoint().setY(nearestReferencePoint.getY());
				}
			}

		} catch (InvalidArgumentException e) {
			// won't reach here
			e.printStackTrace();
		}

	}

	Point getNearestExtremePoint(Arc arc, Point point)
			throws NullArgumentException {

		double distanceToInitial = Geometrics.calculateDistance(point, arc
				.getInitialPoint());
		double distanceToEnding = Geometrics.calculateDistance(point, arc
				.getEndingPoint());

		if (distanceToEnding <= distanceToInitial) {
			return arc.getEndingPoint();
		} else {
			return arc.getInitialPoint();
		}
	}

	/*
	 * private MoveCommand computeExtend(Line line, Point point) { MoveCommand
	 * stretchCommand = null; Point toMove = null; if (line != null) { try {
	 * toMove = getNearestExtremePoint(line, point); } catch
	 * (NullArgumentException e) { // Should never happen e.printStackTrace(); }
	 * } if (toMove != null) { Point intersectionPoint =
	 * getNearestIntersection(line, toMove); if (intersectionPoint != null) {
	 * try { } catch (NullArgumentException e) { // Should never happen
	 * e.printStackTrace(); } } } return stretchCommand; } private Point
	 * getNearestIntersection(Element element, Point point) { PointSortable
	 * sortableElement = null; Point nearestIntersection = null; try {
	 * sortableElement = (PointSortable) element; } catch (ClassCastException e)
	 * { } if (sortableElement != null) { Collection<Point> intersectionPoints =
	 * new ArrayList<Point>(); for (Element reference : references) { try {
	 * Collection<Point> intersection = element .getIntersection(reference); for
	 * (Point intersect : intersection) { if (reference.contains(intersect) &&
	 * !reference.equals(element)) { intersectionPoints.add(intersect); } } }
	 * catch (NullArgumentException e) { // Should not happen
	 * e.printStackTrace(); } } SortedSet<ComparablePoint> sortedPointSet =
	 * sortableElement .getSortedPointSet(point, intersectionPoints);
	 * ComparablePoint extendPoint = null; try { extendPoint = new
	 * ComparablePoint(point, new DoubleKey(0.0)); } catch
	 * (NullArgumentException e) { // Should never happen e.printStackTrace(); }
	 * SortedSet<ComparablePoint> negativeIntersections = sortedPointSet
	 * .headSet(extendPoint); if (negativeIntersections.size() > 0) {
	 * nearestIntersection = negativeIntersections.last().getPoint(); } } return
	 * nearestIntersection; }
	 */

}
