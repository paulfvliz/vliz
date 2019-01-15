package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.dao.LocalVectorLayersDaoImpl;
import be.vliz.emodnet.querytool.core.dao.VectorLayersDao;
import be.vliz.emodnet.querytool.core.model.feature.Feature;
import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class StatisticsTest {
	private Statistics statistics;

	@Before
	public void setUp() throws Exception {
		VectorLayersDao dao = new LocalVectorLayersDaoImpl(StatisticsTest.class.getResourceAsStream("/small_geo.json"));
		FeatureCollection fc = dao.getFeatures(new Rectangle(0,0,90,90), null);

		this.statistics = Statistics.builder()
			.dividingProperty("country")
			.add(fc)
			.build();
	}

	@Test
	public void testBuilder() {
		assertThat(statistics.getStatistics().size(), equalTo(1));
	}

	@Test
  public void testMerge() {
	  Statistics.Builder builder = Statistics.builder()
      .of(statistics)
      .add(statistics);
	  assertThat(builder.build().getTotal(), equalTo(statistics.getTotal() * 2));
  }

	@Test
	public void testImmutability() {
		Statistics s = Statistics.builder().of(statistics).build();
		assertThat(s.getTotal(), equalTo(statistics.getTotal()));
		assertThat(s.getStatistics(), not(sameInstance(statistics.getStatistics())));
		for (String statName : s.getStatistics().keySet()) {
			assertThat(s.getStatistics().get(statName), not(sameInstance(statistics.getStatistics().get(statName))));
		}
	}

}
