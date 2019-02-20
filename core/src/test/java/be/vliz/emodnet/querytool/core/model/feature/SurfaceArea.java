package be.vliz.emodnet.querytool.core.model.feature;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static  org.hamcrest.Matchers.*;

public class SurfaceArea {

	@SuppressWarnings("static-method")
	@Test
	public void test() {
		
		
		Point  p0 = new Point(45.434623, -4.43505);
		Point  p1 = new Point(45.433373, -4.441717);
		Point  p2 = new Point(45.437539, -4.439634);
		Point  p3 = new Point(45.434623, -4.43505);
		List<Point> listp = new ArrayList<Point>();
		listp.add(p0);
		listp.add(p1);
		listp.add(p2);
		listp.add(p3);
		
		Polygon poly = new Polygon(p0, p1, p2,p3);
		
		Integer surArea = (int) poly.surfaceArea();
		System.out.println(surArea); // output is in meters
		
		
		assert(surArea.equals(109438)); 
		// https://geographiclib.sourceforge.io/cgi-bin/Planimeter 
		// polygon & rhumb line, our calculation(using leaflets algorithm) is 109438 theirs is 109450, close enough
	}

	@Test
	public void testRectangle() {
	  double minLat = 52.0;
	  double maxLat = 53.0;
	  double minLon = -3.0;
	  double maxLon = -2.0;
    Point  p0 = new Point(minLat, minLon);
    Point  p1 = new Point(minLat, maxLon);
    Point  p2 = new Point(maxLat, maxLon);
    Point  p3 = new Point(maxLat, minLon);
    List<Point> listp = new ArrayList<Point>();
    listp.add(p0);
    listp.add(p1);
    listp.add(p2);
    listp.add(p3);

    Polygon poly = new Polygon(p0, p1, p2,p3);

    Rectangle r = new Rectangle(minLat, minLon, maxLat, maxLon);

    assertThat(r.surfaceArea(), closeTo(poly.surfaceArea(), 0.1));
  }

}
