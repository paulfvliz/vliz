package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import be.vliz.emodnet.querytool.core.model.FeatureType;

public interface VectorLayersDao {
	FeatureCollection getFeatures(Rectangle bbox, FeatureType type);
}
