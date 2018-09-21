package be.vliz.opensealab.main;

import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.feature.SurfaceCount;

public interface LayerProvider {

	FeatureCollection retrieve(Rectangle bbox, String type, String dividingProperty, boolean cacheOnly, String geomType);

	SurfaceCount retrieveStats(Rectangle bbox, String type, String dividingProperty, String geomType);

}
