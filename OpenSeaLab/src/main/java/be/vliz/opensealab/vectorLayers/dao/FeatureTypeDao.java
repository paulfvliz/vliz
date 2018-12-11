package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.exceptions.BizzException;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import be.vliz.opensealab.vectorLayers.model.Layer;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

@Contract
public interface FeatureTypeDao {
	FeatureType getFeatureType(Layer layer, String type) throws NoSuchElementException;
	Collection<FeatureType> getFeatureTypes(Layer layer) throws BizzException;
}
