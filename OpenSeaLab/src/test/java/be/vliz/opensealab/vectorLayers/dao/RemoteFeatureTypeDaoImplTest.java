package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import be.vliz.opensealab.vectorLayers.model.Layer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

public class RemoteFeatureTypeDaoImplTest {
	private RemoteFeatureTypeDaoImpl dao;
	private LayerDao layerDao;

	@Before
	public void setUp() throws Exception {
		this.dao = new RemoteFeatureTypeDaoImpl();
		layerDao = mock(LayerDao.class);
		when(layerDao.getLayer(anyString())).thenReturn(new Layer(
			"test",
			"http://77.246.172.208/geoserver/emodnet/wfs?service=WFS&version=1.1.0&request={requestType}&typeName={type}&srsName=EPSG:4326&bbox={bbox}&outputFormat=application/json",
			"defaultType",
			"defaultDividor",
			new Rectangle(0, 0, 0, 0)
		));
	}

	@Test
	public void getFeatureType() {
		FeatureType type = dao.getFeatureType(layerDao.getLayer("blook"), "emodnet:windfarms");
		assertThat(type, is(not(nullValue())));
	}

	@Test
	public void getFeatureTypes() {
		Collection<FeatureType> types = dao.getFeatureTypes(layerDao.getLayer("bloop"));
		assertThat(types.size(), is(greaterThan(0)));
		types = dao.getFeatureTypes(layerDao.getLayer("bloop"));
		assertThat(types.size(), is(greaterThan(0)));
	}
}
