package be.vliz.emodnet.querytool.main;

import be.vliz.emodnet.querytool.feature.FeatureCollection;
import be.vliz.emodnet.querytool.feature.Rectangle;
import be.vliz.emodnet.querytool.feature.SurfaceCount;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;

public interface LayerProvider {

	FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, boolean cacheOnly, String geomType);

	SurfaceCount retrieveStats(Rectangle bbox, FeatureType type, String dividingProperty, String geomType);

}
