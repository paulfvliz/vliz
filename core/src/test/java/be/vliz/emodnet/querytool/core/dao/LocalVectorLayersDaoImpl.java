package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.exceptions.FatalException;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.FeatureCollectionBuilder;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class LocalVectorLayersDaoImpl implements VectorLayersDao {
	private InputStream in;
	public LocalVectorLayersDaoImpl(InputStream is) {
		this.in = is;
	}

	@Override
	public FeatureCollection getFeatures(Rectangle bbox, FeatureType type) {
		try {
			FeatureCollection fc = new FeatureCollectionBuilder(new InputStreamReader(in, StandardCharsets.UTF_8.name())).create();
			fc = fc.clippedWith(bbox);
			return fc;
		} catch (UnsupportedEncodingException e) {
			throw new FatalException(e);
		}
	}
}
