package be.vliz.opensealab.feature;

import java.util.Arrays;
import java.util.List;

public class LineString extends Geometry {
	private static final long serialVersionUID = 1L;
	private final List<Point> points;

	public LineString(Point... points) {
		this(Arrays.asList(points));
	}

	public LineString(List<Point> points) {
		super("LineString");
		this.points = points;
	}

	@Override
	public String getCoordinates() {
		StringBuilder sb = new StringBuilder();
		for (Point p : points) {
			sb.append("[");
			sb.append(p.getCoordinates() + ", ");
			sb.append("]");
		}
		return sb.delete(sb.length() - 2, sb.length()).toString();
	}

	@Override
	public double surfaceArea() {
		return 0;
	}

	@Override
	public Geometry clippedWith(Rectangle r) {
		// TODO Auto-generated method stub
		return null;
	}

}
