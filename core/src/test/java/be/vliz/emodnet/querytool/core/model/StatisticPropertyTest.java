package be.vliz.emodnet.querytool.core.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class StatisticPropertyTest {
	private StatisticProperty.Builder builder;
	private Statistic statistic;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		statistic = mock(Statistic.class);
		builder = StatisticProperty.builder().name("country");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCollectNumber() {
		Integer value = 5;
    NumbericStatisticProperty sp = (NumbericStatisticProperty)builder.add(value, null).build(statistic);
		assertThat(sp.getTotal(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testCollectNonNumber() {
		builder.add("hh", null);
		//thrown.expect(IllegalStateException.class);
    assertThat(((DistinctCountStatisticProperty)builder.build(statistic)).getDistinctCounts().size(), greaterThan(0));
	}

	@Test
	public void testGetMin() {
		Integer value = 5;
		builder.add(value, null).add(value * 2, null);
		assertThat(((NumbericStatisticProperty)builder.build(statistic)).getMin(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testGetMax() {
		Integer value = 5;
		builder.add(value, null).add(value * 2, null);
		assertThat(((NumbericStatisticProperty)builder.build(statistic)).getMax(), closeTo(value.doubleValue() * 2.0d, 0.000001));
	}

	@Test
	public void testGetTotal() {
		Integer value = 5;
		Integer it = 1000;
		for (int i = 0; i < it; i++) {
			builder.add(value, null);
		}
		assertThat(((NumbericStatisticProperty)builder.build(statistic)).getTotal(), closeTo(value.doubleValue() * it.doubleValue(), 0.000001));
	}

	@Test
	public void testGtAvg() {
		Integer value = 5;
		when(statistic.getTotal()).thenReturn(1000.0d);
		for (int i = 0; i < statistic.getTotal(); i++) {
			builder.add(value, null);
		}
		assertThat(((NumbericStatisticProperty)builder.build(statistic)).getAvg(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testValidity() {
		assertFalse(builder.isValid());
		builder
			.add(4, null)
			.add(5, null);
		assertTrue(builder.isValid());
	}

	@Test
	public void testValidityOneValue() {
		assertThat(builder.isValid(), is(false));
		assertThat(builder.add(1, null).isValid(), is(true));

	}

}
