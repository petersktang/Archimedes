package br.org.archimedes.intersectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.org.archimedes.Tester;
import br.org.archimedes.ellipse.Ellipse;
import br.org.archimedes.exceptions.InvalidArgumentException;
import br.org.archimedes.exceptions.NullArgumentException;
import br.org.archimedes.infiniteline.InfiniteLine;
import br.org.archimedes.model.Point;

public class EllipseInfiniteLineIntersectorTest extends Tester {

	// TODO Create a test using an ellipse outside the origin
	// TODO Create a test using a rotated ellipse
	private Ellipse ellipse1x1, ellipse2x2, ellipse2x1, ellipse2x1T,
			ellipse2x3;
	private EllipseInfiniteLineIntersector intersector;

	@Before
	public void SetUp() throws NullArgumentException, InvalidArgumentException {
		ellipse1x1 = new Ellipse(new Point(0, 0), new Point(1, 0), new Point(0,
				1));
		ellipse2x2 = new Ellipse(new Point(0, 0), new Point(2, 0), new Point(0,
				2));
		ellipse2x1 = new Ellipse(new Point(0, 0), new Point(2, 0), new Point(0,
				1));
		ellipse2x1T = new Ellipse(new Point(-4, 2), new Point(-2, 2),
				new Point(-4, 3));
		ellipse2x3 = new Ellipse(new Point(-4, 2), new Point(
				-1.401923788646684, 3.5), new Point(-5, 3.732050807568877));

		intersector = new EllipseInfiniteLineIntersector();
	}

	@Test
	public void shouldNotReturnIntersectionPoints()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(3.0, 0.0, 3.0, 1.0);
		assertCollectionTheSame(new ArrayList<Point>(),
				intersector.getIntersections(ellipse1x1, verticalLine));
		assertCollectionTheSame(new ArrayList<Point>(),
				intersector.getIntersections(ellipse2x2, verticalLine));
		assertCollectionTheSame(new ArrayList<Point>(),
				intersector.getIntersections(ellipse2x1, verticalLine));
		assertCollectionTheSame(new ArrayList<Point>(),
				intersector.getIntersections(ellipse2x1T, verticalLine));
		assertCollectionTheSame(new ArrayList<Point>(),
				intersector.getIntersections(ellipse2x3, verticalLine));
	}

	@Test
	public void shouldReturnOneTangencyPoint1x1()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(1.0, 0.0, 1.0, 1.0);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints.add(new Point(1.0, 0.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse1x1, verticalLine));

		InfiniteLine horizontalLine = new InfiniteLine(0.0, 1.0, 1.0, 1.0);

		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(0.0, 1.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse1x1, horizontalLine));
	}

	@Test
	public void shouldReturnOneTangencyPoint2x2()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(2.0, 0.0, 2.0, 1.0);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints.add(new Point(2.0, 0.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x2, verticalLine));

		InfiniteLine horizontalLine = new InfiniteLine(0.0, 2.0, 1.0, 2.0);

		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(0.0, 2.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x2, horizontalLine));
	}

	@Test
	public void shouldReturnOneTangencyPoint2x1()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(2.0, 0.0, 2.0, 1.0);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints.add(new Point(2.0, 0.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1, verticalLine));

		InfiniteLine horizontalLine = new InfiniteLine(0.0, 1.0, 1.0, 1.0);

		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(0.0, 1.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1, horizontalLine));
	}
	
	@Test
	public void shouldReturnOneTangencyPoint2x1T()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(-2.0, 0.0, -2.0, 1.0);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints.add(new Point(-2.0, 2.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1T, verticalLine));

		InfiniteLine horizontalLine = new InfiniteLine(-4.0, 3.0, -5.0, 3.0);

		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(-4.0, 3.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1T, horizontalLine));
	}

	@Test
	public void shouldReturnOneDiagonalTangencyPoint1x1()
			throws InvalidArgumentException, NullArgumentException {
		double sqrt2 = Math.sqrt(2);
		InfiniteLine diagonalLine = new InfiniteLine(sqrt2, 0.0, 0.0, sqrt2);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(sqrt2 / 2, sqrt2 / 2));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse1x1, diagonalLine));
	}

	@Test
	public void shouldReturnOneDiagonalTangencyPoint2x2()
			throws InvalidArgumentException, NullArgumentException {
		double sqrt2 = Math.sqrt(2);
		InfiniteLine diagonalLine = new InfiniteLine(2 * sqrt2, 0.0, 0.0,
				2 * sqrt2);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(sqrt2, sqrt2));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x2, diagonalLine));
	}

	@Test
	public void shouldReturnOneDiagonalTangencyPoint2x3()
			throws InvalidArgumentException, NullArgumentException {
		InfiniteLine diagonalLine = new InfiniteLine(-1.0, 0.0, -4.7069360559627, -0.28210643262392987);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(-4.7069360559627, -0.28210643262392987));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x3, diagonalLine));
	}

	@Test
	public void shouldReturnOneDiagonalTangencyPoint2x1()
			throws InvalidArgumentException, NullArgumentException {

		double sqrt3 = Math.sqrt(3.0);
		InfiniteLine diagonalLine = new InfiniteLine(4, 0.0, 1.0, sqrt3 / 2.0);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(1, sqrt3 / 2.0));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1, diagonalLine));
	}

	@Test
	public void shouldReturnOneDiagonalTangencyPoint2x1T()
			throws InvalidArgumentException, NullArgumentException {

		double sqrt3 = Math.sqrt(3.0);
		InfiniteLine diagonalLine = new InfiniteLine(4 -4, 0.0 +2, 1.0 -4, (sqrt3 / 2.0) +2);

		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints = new ArrayList<Point>();
		interPoints.add(new Point(1 - 4, sqrt3 / 2.0 + 2));

		assertCollectionTheSame(interPoints,
				intersector.getIntersections(ellipse2x1T, diagonalLine));
	}

	
	@Test
	public void shouldReturnTwoTangecyPoint() throws InvalidArgumentException,
			NullArgumentException {
		InfiniteLine verticalLine = new InfiniteLine(0.0, 0.0, 0.0, 1.0);
		InfiniteLine horizontalLine = new InfiniteLine(0.0, 0.0, 1.0, 0.0);

		assertCollectionTheSame(generateList(0.0, -1.0, 0.0, 1.0),
				intersector.getIntersections(ellipse1x1, verticalLine));

		assertCollectionTheSame(generateList(-1.0, 0.0, 1.0, 0.0),
				intersector.getIntersections(ellipse1x1, horizontalLine));

		assertCollectionTheSame(generateList(0.0, -2.0, 0.0, 2.0),
				intersector.getIntersections(ellipse2x2, verticalLine));

		assertCollectionTheSame(generateList(-2.0, 0.0, 2.0, 0.0),
				intersector.getIntersections(ellipse2x2, horizontalLine));

		assertCollectionTheSame(generateList(0.0, -1.0, 0.0, 1.0),
				intersector.getIntersections(ellipse2x1, verticalLine));

		assertCollectionTheSame(generateList(-2.0, 0.0, 2.0, 0.0),
				intersector.getIntersections(ellipse2x1, horizontalLine));

		assertCollectionTheSame(
				generateList(0.625, 0.9499177595981665, 1.3, 0.7599342076785331),
				intersector.getIntersections(ellipse2x1, new InfiniteLine(4,
						0.0, 1.3, Math.sqrt(2.31) / 2.0)));
	}

	private ArrayList<Point> generateList(double x1, double y1, double x2,
			double y2) {
		ArrayList<Point> interPoints = new ArrayList<Point>();
		interPoints.add(new Point(x1, y1));
		interPoints.add(new Point(x2, y2));
		return interPoints;
	}

	@Test
    public void shouldReturnTwoIntersectionPointRotated() throws InvalidArgumentException, NullArgumentException
    {
		InfiniteLine diagonalLine = new InfiniteLine(-8.0, 3.0, -2.0, -1.0);
        Collection<Point> result = intersector.getIntersections(ellipse2x3, diagonalLine);
        System.out.println(result);
        
        List<Point> intersectionPoints = new ArrayList<Point>();
        intersectionPoints.add(new Point(-3.517944219543928,0.011962813029285257));
        intersectionPoints.add(new Point(-6.5922956062404285,2.061530404160286));
        assertCollectionTheSame(intersectionPoints, result);
    }
	
	
}
