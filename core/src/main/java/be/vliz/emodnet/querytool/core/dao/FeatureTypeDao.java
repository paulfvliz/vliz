package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.exceptions.BizzException;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.Layer;
import org.jvnet.hk2.annotations.Contract;

import java.util.Collection;
import java.util.NoSuchElementException;

@Contract
public interface FeatureTypeDao {
	FeatureType getFeatureType(Layer layer, String type) throws NoSuchElementException;
	Collection<FeatureType> getFeatureTypes(Layer layer) throws BizzException;
}
