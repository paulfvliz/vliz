package feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiPolygon extends Geometry {

	private static final long serialVersionUID = 1L;
	private final List<Polygon> polygons;
	private final List<Polygon> exteriorRings = new ArrayList<>();

	public MultiPolygon(Polygon... polygons) {
		this(Arrays.asList(polygons));
	}

	public MultiPolygon() {
		this(new ArrayList<>());
	}

	public MultiPolygon(List<Polygon> polygons) {
		super("MultiPolygon");
		this.polygons = polygons;
	}

	public void addExteriorPolygon(Polygon polygon) {
		this.exteriorRings.add(polygon);
	}

	@Override
	public String getCoordinates() {
		StringBuilder sb = new StringBuilder();
		// Main ring with holes
		if (polygons.size() > 0) {
			sb.append("[");
			for (Polygon p : polygons) {
				sb.append("\n");
				sb.append(p.getCoordinates());
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			sb.append("], ");
		}

		if (exteriorRings.size() > 0) {
			// Extra exterior rings
			for (Polygon polygon : exteriorRings) {
				sb.append("\n[");
				sb.append(polygon.getCoordinates());
				sb.append("], ");
			}
		}

		return sb.delete(sb.length() - 2, sb.length()).toString();
	}

	@Override
	public double surfaceArea() {
		double area = 0;
		for (Polygon polygon : polygons) {
			area += polygon.surfaceArea();
		}
		return area;
	}

	@Override
	public MultiPolygon clippedWith(Rectangle r) {
		List<Polygon> newPolyes = new ArrayList<>();
		for (Polygon polygon : polygons) {
			Polygon n = polygon.clippedWith(r);
			if (n != null) {
				newPolyes.add(n);
			}
		}
		if (newPolyes.isEmpty()) {
			return null;
		}
		MultiPolygon mp = new MultiPolygon(newPolyes);
		return mp;
	}

}
