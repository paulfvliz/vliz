package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import be.vliz.emodnet.querytool.core.model.Layer;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class PropertiesLayerDaoImpl implements LayerDao {
	private Properties properties;

	@Inject
	public PropertiesLayerDaoImpl(@Named("app-properties") Properties properties) {
		this.properties = properties;
	}

	@Override
	public Layer getLayer(String layername) {
		return getLayers().stream().filter((layer -> layername.equals(layer.getName()))).findFirst().get();
	}

	@Override
	public Collection<Layer> getLayers() {
		return Arrays.stream(properties.getProperty("known-layers").split(";"))
			.map((String layerName) ->  new Layer(
					layerName,
					properties.getProperty(layerName),
					properties.getProperty(layerName + "-default-type"),
					properties.getProperty(layerName + "-default-dividor"),
					new Rectangle(
						properties.getProperty(layerName + "-min-lat"),
						properties.getProperty(layerName + "-min-lon"),
						properties.getProperty(layerName + "-max-lat"),
						properties.getProperty(layerName + "-max-lon")
					)
				)
		).collect(Collectors.toCollection(LinkedList::new));
	}
}
