package be.vliz.opensealab.feature;

import java.io.Serializable;

public abstract class Geometry implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String type;

	public Geometry(String type) {
		this.type = type;
	}

	public abstract String getCoordinates();
	public abstract double surfaceArea();
	
	/**
	 * Returns a clipped version of this geometry.
	 * 
	 * MIGHT BE NULL if not within bounds!
	 * @param minLat
	 * @param minLong
	 * @param maxLat
	 * @param maxLong
	 * @return
	 */
	public abstract Geometry clippedWith(Rectangle r);

	/**
	 * Gives the "geometry"-part of the geojson
	 * 
	 * @param geo
	 * @return
	 */
	public String toGeoJSON() {
		StringBuilder result = new StringBuilder();
		result.append("\"geometry\": { \"type\": \"");
		result.append(type);
		result.append("\", \"coordinates\": ");
		result.append(getCoordinates());
		return result.append(" }").toString();
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return this.toGeoJSON();
	}	

}
