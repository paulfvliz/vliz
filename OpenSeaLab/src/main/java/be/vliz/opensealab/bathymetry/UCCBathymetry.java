package be.vliz.opensealab.bathymetry;

import java.io.File;
import java.io.Serializable;

import be.vliz.opensealab.feature.Rectangle;

public class UCCBathymetry implements Serializable {
	private static final long serialVersionUID = 7748691966036755859L;
	private BathymetryDAO bathymetryDAO;
	
	public UCCBathymetry(BathymetryDAO bathymetryDAO) {
		this.bathymetryDAO = bathymetryDAO;
	}
	
	public File getStats(Rectangle bbox) {
		return bathymetryDAO.getStats(bbox);
	}
	
}
