package be.vliz.emodnet.querytool.bathymetry;

import be.vliz.emodnet.querytool.feature.Rectangle;

import java.io.File;
import java.io.Serializable;

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
