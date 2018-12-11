package be.vliz.opensealab.main;

import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.feature.SurfaceCount;
import be.vliz.opensealab.vectorLayers.model.FeatureType;

import java.io.Serializable;

public interface LayerProvider {

	FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, boolean cacheOnly, String geomType);

	SurfaceCount retrieveStats(Rectangle bbox, FeatureType type, String dividingProperty, String geomType);

}
