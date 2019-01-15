package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.model.Statistics;
import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import be.vliz.emodnet.querytool.core.model.feature.SurfaceCount;
import be.vliz.emodnet.querytool.core.model.FeatureType;

public interface LayerProvider {

	FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, boolean cacheOnly, String geomType);

	Statistics retrieveStats(Rectangle bbox, FeatureType type, String dividingProperty, String geomType);

}
