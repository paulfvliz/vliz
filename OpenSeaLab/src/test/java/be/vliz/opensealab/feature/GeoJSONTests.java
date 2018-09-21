package feature;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class GeoJSONTests {

	@SuppressWarnings("static-method")
	@Test
	public void test() {
		Point  p0 = new Point(51.123, 4.123);
		assertThat(p0.toGeoJSON(), equalTo("\"geometry\": { \"type\": \"Point\", \"coordinates\": [[4.123, 51.123]] }"));
		Point  p1 = new Point(51.345, 4.567);
		Point  p2 = new Point(51.678, 4.890);

		Polygon p = new Polygon(p0, p1, p2);

		assertThat(p.toGeoJSON(), equalTo("\"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[4.123, 51.123], [4.567, 51.345], [4.89, 51.678]]] }"));
		
		Feature f = new Feature();
		f.setBbox(new Point[] {p0, p1});
		f.setGeometry(p);
		
		assertThat(f.toGeoJSON(), equalTo("{ \"type\": \"Feature\", \"bbox\": [51.123,4.123,51.345,4.567],\n\"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[4.123, 51.123], [4.567, 51.345], [4.89, 51.678]]] }\n, \"properties\": {}}"));
		
		FeatureCollection fc = new FeatureCollection();
		fc.addFeature(f);
		System.out.println(fc.toGeoJSON());
		
	}

}
