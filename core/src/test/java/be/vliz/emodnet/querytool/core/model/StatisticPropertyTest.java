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
		StatisticProperty sp = builder.add(value).build(statistic);
		assertThat(sp.getTotal(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testCollectNonNumber() {
		builder.add("hh");
		thrown.expect(IllegalStateException.class);
		builder.build(statistic);
	}

	@Test
	public void testGetMin() {
		Integer value = 5;
		builder.add(value).add(value * 2);
		assertThat(builder.build(statistic).getMin(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testGetMax() {
		Integer value = 5;
		builder.add(value).add(value * 2);
		assertThat(builder.build(statistic).getMax(), closeTo(value.doubleValue() * 2.0d, 0.000001));
	}

	@Test
	public void testGetTotal() {
		Integer value = 5;
		Integer it = 1000;
		for (int i = 0; i < it; i++) {
			builder.add(value);
		}
		assertThat(builder.build(statistic).getTotal(), closeTo(value.doubleValue() * it.doubleValue(), 0.000001));
	}

	@Test
	public void testGtAvg() {
		Integer value = 5;
		when(statistic.getTotal()).thenReturn(1000.0d);
		for (int i = 0; i < statistic.getTotal(); i++) {
			builder.add(value);
		}
		assertThat(builder.build(statistic).getAvg(), closeTo(value.doubleValue(), 0.000001));
	}

	@Test
	public void testValidity() {
		assertFalse(builder.isValid());
		builder
			.add(4)
			.add(5);
		assertTrue(builder.isValid());
	}

	@Test
	public void testValidityOneValue() {
		assertThat(builder.isValid(), is(false));
		assertThat(builder.add(1).isValid(), is(true));

	}

}
