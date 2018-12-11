package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.exceptions.BizzException;
import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.vectorLayers.model.FeatureType;

public interface VectorLayersDao {
	FeatureCollection getFeatures(Rectangle bbox, FeatureType type);
}
