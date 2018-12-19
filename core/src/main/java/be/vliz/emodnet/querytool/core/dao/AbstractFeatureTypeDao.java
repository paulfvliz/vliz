package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.exceptions.BizzException;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.Layer;

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
