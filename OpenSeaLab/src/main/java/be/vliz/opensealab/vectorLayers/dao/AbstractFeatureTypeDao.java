package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.exceptions.BizzException;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import be.vliz.opensealab.vectorLayers.model.Layer;

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
