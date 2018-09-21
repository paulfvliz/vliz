package be.vliz.opensealab.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import be.vliz.opensealab.exceptions.FatalException;
import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.FeatureCollectionBuilder;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.feature.SurfaceCount;

/**
 * Mainly meant for testing
 */
public class FromJSONFileLayer implements LayerProvider {
	private final FeatureCollection fc;

	public FromJSONFileLayer(String file) throws IOException {
		String geojson = String.join("\n", Files.readAllLines(Paths.get(file)));
		FeatureCollectionBuilder fcb = new FeatureCollectionBuilder(geojson);
		fc = fcb.create();
	}

	@Override
	public FeatureCollection retrieve(Rectangle bbox, String type, String dividingProperty, boolean cacheOnly,
			String geomType) {
		return fc;
	}

	@Override
	public SurfaceCount retrieveStats(Rectangle bbox, String type, String dividingProperty, String geomType) {
		throw new FatalException("Not supported");
	}

}
