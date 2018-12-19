package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.model.Layer;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class PropertiesLayerDaoImplTest {
	private PropertiesLayerDaoImpl dao;
	@Before
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.load(PropertiesLayerDaoImplTest.class.getResourceAsStream("test.properties"));
		dao = new PropertiesLayerDaoImpl(properties);
	}

	@Test
	public void getLayer() {
		String layername = "human";
		Layer layer = dao.getLayer(layername);
		assertThat(layer, is(not(nullValue())));
		assertThat(layer.getName(), equalTo(layername));
	}

	@Test
	public void getLayers() {
		Collection<Layer> layers = dao.getLayers();
		assertThat(layers.size(), greaterThan(0));
	}
}
