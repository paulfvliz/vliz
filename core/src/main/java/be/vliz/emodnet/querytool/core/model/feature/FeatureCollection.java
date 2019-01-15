package be.vliz.emodnet.querytool.core.model.feature;

import be.vliz.emodnet.querytool.core.model.Statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureCollection implements Serializable {
	private static final long serialVersionUID = 2257120673609525062L;
	private final List<Feature> features;

	public FeatureCollection(List<Feature> features) {
		this.features = features;
	}

	public FeatureCollection() {
		features = new ArrayList<>();
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void addFeature(Feature f) {
		features.add(f);
	}

	public String toGeoJSON() {
		if (features.size() == 0) {
			return "{ \"type\": \"FeatureCollection\", \"features\":[]}";
		}
		StringBuilder result = new StringBuilder();
		result.append("{ \"type\": \"FeatureCollection\", \"features\": \n [");
		for (Feature f : features) {
			result.append(f.toGeoJSON());
			result.append(", \n");
		}
		result.delete(result.length() - 3, result.length());
		return result.append("]\n}").toString();
	}

	@Override
	public String toString() {
		return toGeoJSON();
	}

	public FeatureCollection clippedWith(Rectangle r) {
		List<Feature> features = new ArrayList<>();
		for (Feature feature : this.features) {
			Feature f = feature.clippedWith(r);
			if (f != null) {
				features.add(f);
			}
		}
		return new FeatureCollection(features);
	}

	public Statistics calculateTotals(String dividingPropery) {
		return Statistics.builder()
			.dividingProperty(dividingPropery)
			.add(this)
			.build();
	}

	public FeatureCollection joinWith(FeatureCollection fc) {
		if (fc == null) {
			return this;
		}
		List<Feature> feats = new ArrayList<>(this.features.size() + fc.features.size());
		feats.addAll(this.features);
		feats.addAll(fc.features);
		return new FeatureCollection(feats);
	}

	public void remove(Feature feature) {
		features.remove(feature);
	}

	public void removeLast() {
		features.remove(features.size() - 1);
	}

}
