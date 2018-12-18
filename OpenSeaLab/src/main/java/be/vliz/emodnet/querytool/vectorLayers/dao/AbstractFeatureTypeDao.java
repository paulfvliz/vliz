package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.exceptions.BizzException;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;
import be.vliz.emodnet.querytool.vectorLayers.model.Layer;

import java.util.Collection;

public abstract class AbstractFeatureTypeDao implements FeatureTypeDao {
	@Override
	public FeatureType getFeatureType(Layer layer, String type) throws BizzException {
		Collection<FeatureType> featureTypeCollection = getFeatureTypes(layer);
		return featureTypeCollection.stream()
				.filter(featureType -> type.equals(featureType.getName()))
				.findFirst()
				.get();
	}
}
