package be.vliz.emodnet.querytool.feature;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.*;

import com.owlike.genson.Genson;
import org.junit.Test;

public class ClippingTest {
	
	@SuppressWarnings("static-method")
	@Test
	public void testClipping() throws Exception {
		Rectangle r = new Rectangle(0, 0, 1, 2);
		Point p = new Point(0.5, 0.5);
		assertTrue(r.containsPoint(p));
		assertTrue(p.clippedWith(r) != null);
		
		
		Polygon poly = new Rectangle(0.1, 0.1, 0.9, 0.9).asPolygon();
		Polygon poly0 = poly.clippedWith(r);
		assertThat(poly.toGeoJSON(), equalTo(poly0.toGeoJSON()));
		
		Polygon poly1 = new Rectangle(0.1, 0.1, 0.9, 2.1).asPolygon().clippedWith(r);
		String expected = "\"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[0.1, 0.1], [2.0, 0.1], [2.0, 0.9], [0.1, 0.9]]] }";
		assertThat(poly1.toGeoJSON(), equalTo(expected));
	}

}
