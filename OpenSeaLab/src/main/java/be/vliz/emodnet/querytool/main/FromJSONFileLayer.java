package be.vliz.emodnet.querytool.main;

import be.vliz.emodnet.querytool.exceptions.FatalException;
import be.vliz.emodnet.querytool.feature.FeatureCollection;
import be.vliz.emodnet.querytool.feature.FeatureCollectionBuilder;
import be.vliz.emodnet.querytool.feature.Rectangle;
import be.vliz.emodnet.querytool.feature.SurfaceCount;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Mainly meant for testing
 */
public class FromJSONFileLayer implements LayerProvider {
	private static final long serialVersionUID = 1L;
	private final FeatureCollection fc;

	public FromJSONFileLayer(String file) throws IOException {
		String geojson = String.join("\n", Files.readAllLines(Paths.get(file)));
		FeatureCollectionBuilder fcb = new FeatureCollectionBuilder(geojson);
		fc = fcb.create();
	}

	@Override
	public FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, boolean cacheOnly,
									  String geomType) {
		return fc;
	}

	@Override
	public SurfaceCount retrieveStats(Rectangle bbox, FeatureType type, String dividingProperty, String geomType) {
		throw new FatalException("Not supported");
	}

}
