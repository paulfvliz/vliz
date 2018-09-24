package be.vliz.opensealab.feature;

public class Square extends Rectangle{
	private static final long serialVersionUID = 1L;
	
	public Square(double lat, double lon) {
		super(lat, lon, lat+1, lon+1);
	}

}
