package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Feature;
import be.vliz.emodnet.querytool.core.model.feature.Geometry;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;


public class StatisticTest {
	private Statistic.Builder statisticBuilder;
	private Feature feature;
	private Statistics statistics;

	private static final double DEFAULT_ERROR = 0.000001d;

  @Rule
  public ExpectedException thrown = ExpectedException.none();


  @Before
	public void setUp() throws Exception {
		statisticBuilder = Statistic.builder().name("stat1");
		feature = mock(Feature.class);
		Geometry g = mock(Geometry.class);
		when(g.getType()).thenReturn("Point");
		when(feature.getGeometry()).thenReturn(g);
		statistics = mock(Statistics.class);
	}

	@Test
	public void testGetTotal() {
		int cnt = 10;
		for(int i = 0; i < cnt; i++) {
			statisticBuilder.add(feature);
		}
		assertThat(statisticBuilder.build(statistics).getTotal(), closeTo(cnt, DEFAULT_ERROR));
	}

	@Test
	public void testGetTotalNull() {
		assertThat(statisticBuilder.build(statistics).getTotal(), closeTo(0.0, DEFAULT_ERROR));
	}

	@Test
	public void testGetRatioNull() {
		when(statistics.getTotal()).thenReturn(10.0);
		assertThat(statisticBuilder.build(statistics).getRatio(), closeTo(0.0, DEFAULT_ERROR));
	}

	@Test
	public void testGetRatio() {
		double total = 50.d;
		double ratio = 0.5d;
		when(statistics.getTotal()).thenReturn(total);
		for(int i = 0; i < total * ratio; i++) {
			statisticBuilder.add(feature);
		}
		assertThat(statisticBuilder.build(statistics).getRatio(), closeTo(ratio, DEFAULT_ERROR));
	}

	@Test
	public void testGetProperties() {
		String propName = "prop1";
		when(feature.getProperties()).thenReturn(ImmutableMap.of(propName, 1.0d));
		statisticBuilder.add(feature);
		assertThat(statisticBuilder.build(statistics).getProperties(),  hasKey(propName));
	}

  @Test
  public void testGetNonNumericProperties() {
    String propName = "prop1";
    when(feature.getProperties()).thenReturn(ImmutableMap.of(propName, "glue"));
    statisticBuilder.add(feature);
    assertThat(statisticBuilder.build(statistics).getProperties(),  hasKey(propName));
  }

	@Test
	public void testGetPropertiesNull() {
		String propName = "prop1";
		String propVal = "not a numberic value";
		when(feature.getProperties()).thenReturn(ImmutableMap.of(propName, propVal));
		statisticBuilder.add(feature);
		Statistic statistic = statisticBuilder.build(statistics);
		assertThat(statistic.getProperties(), hasKey(propName));
		StatisticProperty statisticProperty = statistic.getProperties().get(propName);
		assertThat(statisticProperty, instanceOf(DistinctCountStatisticProperty.class));
		assertThat(((DistinctCountStatisticProperty)statisticProperty).getDistinctCounts().get(propVal), closeTo(1.0, 0.0001));
	}

	@Test
  public void testValidity() {
	  assertThat(Statistic.builder().isValid(), is(false));
    assertThat(Statistic.builder().name("Bleeip").isValid(), is(true));
  }

  @Test
  public void testInvalidBuild() {
	  thrown.expect(IllegalStateException.class);
	  Statistic.builder().build(statistics);
  }

  @Test
  public void testBuildOf() {
    Statistic s = statisticBuilder.add(feature).build(statistics);
    Statistic result = Statistic.builder().of(s).build(statistics);
    assertThat(result.getTotal(), equalTo(s.getTotal()));
    assertThat(result.getName(), equalTo(s.getName()));
    assertThat(result.getRatio(), equalTo(s.getRatio()));
  }
}
