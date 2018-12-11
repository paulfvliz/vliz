package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.vectorLayers.model.Layer;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;
import java.util.NoSuchElementException;

@Contract
public interface LayerDao {
	public Layer getLayer(String layername) throws NoSuchElementException;
	public Collection<Layer> getLayers();

}
