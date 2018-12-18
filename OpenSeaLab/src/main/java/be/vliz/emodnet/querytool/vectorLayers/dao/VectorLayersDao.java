package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.feature.FeatureCollection;
import be.vliz.emodnet.querytool.feature.Rectangle;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;

public interface VectorLayersDao {
	FeatureCollection getFeatures(Rectangle bbox, FeatureType type);
}
