package be.vliz.opensealab.bathymetry;

import java.io.File;

import be.vliz.opensealab.feature.Rectangle;

public class UCCBathymetry {
	private BathymetryDAO bathymetryDAO;
	
	public UCCBathymetry(BathymetryDAO bathymetryDAO) {
		this.bathymetryDAO = bathymetryDAO;
	}
	
	public File getStats(Rectangle bbox) {
		return bathymetryDAO.getStats(bbox);
	}
	
}
