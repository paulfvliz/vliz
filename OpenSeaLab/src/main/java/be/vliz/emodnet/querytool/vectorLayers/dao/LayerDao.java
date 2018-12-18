package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.vectorLayers.model.Layer;
import org.jvnet.hk2.annotations.Contract;

import java.util.Collection;
import java.util.NoSuchElementException;

@Contract
public interface LayerDao {
	public Layer getLayer(String layername) throws NoSuchElementException;
	public Collection<Layer> getLayers();

}
