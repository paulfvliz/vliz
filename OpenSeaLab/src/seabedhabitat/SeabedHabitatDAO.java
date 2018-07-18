package seabedhabitat;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.owlike.genson.Genson;

import exceptions.FatalException;
import main.Util;
import seabedhabitat.feature.FeatureCollection;
import seabedhabitat.feature.Rectangle;

public class SeabedHabitatDAO {
	private static final Logger LOGGER = Logger.getLogger(SeabedHabitatDAO.class.getName());
	private final String url;
	private final String defaultType;

	/**
	 * Constructs a data object access that manages everything linked to pure data.
	 * 
	 * @param url
	 *            webservice url
	 * @param defaultType
	 *            type name of the seabed habitat
	 */
	public SeabedHabitatDAO(String url, String defaultType) {
		this.url = url;
		this.defaultType = defaultType;
	}

	/**
	 * Fetchs, saves and returns a geojson file.
	 * 
	 * @param bbox
	 *            bounding box
	 * @param type
	 *            seabed habitat type
	 * @return geojson file
	 */
	public FeatureCollection getFeatures(Rectangle bbox, String type) {
		FeatureCollection fc;
		try {
			fc = fetch(bbox, type == null ? defaultType : type);
			fc = fc.clippedWith(bbox);
			return fc;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * Fetchs, saves and returns statistics in json format.
	 * 
	 * @param bbox
	 *            bounding box
	 * @param type
	 *            seabed habitat type
	 * @return a file of statistics
	 */
	public String getStats(Rectangle bbox, String type) {
		try {
			FeatureCollection fc = fetch(bbox, type == null ? defaultType : type);
			Map<String, Double> stats = fc.clippedWith(bbox).calculatePercentages();
			return new Genson().serialize(stats);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * Gets the data from the WMS
	 * 
	 * @param bbox
	 * @param type
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private FeatureCollection fetch(Rectangle bbox, String type)
			throws SAXException, IOException, ParserConfigurationException {
		String bx = bbox.getMinLon() + "," + bbox.getMinLat() + "," + bbox.getMaxLon() + "," + bbox.getMaxLat();
		LOGGER.log(Level.FINE, "Querying WMS server");
		String URL = url.replace("{bbox}", bx).replace("{type}", type);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		SAXHandler userhandler = new SAXHandler();
		saxParser.parse(Util.fetchFrom(URL), userhandler);
		LOGGER.log(Level.FINE, "Got result for bbox: " + bx);
		return userhandler.getFeatures();
	}

}
