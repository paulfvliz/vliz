package be.vliz.emodnet.querytool.feature;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.owlike.genson.Genson;

public class Feature implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String type = "Feature";
	
	private Point[] bbox = new Point[2];
	private Geometry geometry;
	private Map<String, Object> properties = new HashMap<>();
	
	public Feature() {
	}
	
	public Feature(Geometry geometry, Map<String, Object> properties, Point[] bbox) {
		this.geometry = geometry;
		this.properties = properties;
	}
	
	public Point[] getBbox() {
		return Arrays.copyOf(bbox, bbox.length);
	}

	public void setBbox(Point[] bbox) {
		this.bbox = Arrays.copyOf(bbox, bbox.length);
	}

	public void setBboxLower(Point lower) {
		this.bbox[0] = lower;
	}

	public void setBboxUpper(Point upper) {
		this.bbox[1] = upper;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getType() {
		return type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String toGeoJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"type\": \"");
		sb.append(type + "\", ");
		if (bbox[0] != null) {
			sb.append("\"bbox\": [");
			sb.append(
					bbox[0].getLat() + "," + bbox[0].getLon() + "," + bbox[1].getLat() + "," + bbox[1].getLon() + "],");
		}
		sb.append("\n");
		if (geometry != null) {
			sb.append(geometry.toGeoJSON());
			sb.append("\n");
		} else {
			sb.append("\"geometry\": null");
		}
		sb.append(", \"properties\": ");
		sb.append(new Genson().serialize(properties) + "}");
		return sb.toString();
	}

	@Override
	public String toString() {
		return toGeoJSON();
	}

	public Feature clippedWith(Rectangle r) {
		Geometry newGeo = this.geometry.clippedWith(r);
		if(newGeo == null) {
			return null;
		}
		return new Feature(newGeo, this.properties, this.bbox);
	}

	public Feature copy() {
		Feature f = new Feature();
		f.bbox = this.bbox;
		f.properties = this.properties;
		f.geometry = this.geometry;
		return f;
	}

}
