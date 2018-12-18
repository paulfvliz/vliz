package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.exceptions.FatalException;
import be.vliz.emodnet.querytool.feature.FeatureCollection;
import be.vliz.emodnet.querytool.feature.FeatureCollectionBuilder;
import be.vliz.emodnet.querytool.feature.Rectangle;
import be.vliz.emodnet.querytool.main.Util;
import be.vliz.emodnet.querytool.vectorLayers.FeatureSaxHandler;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;
import be.vliz.emodnet.querytool.vectorLayers.model.Layer;
import org.eclipse.jetty.util.URIUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteVectorLayersDaoImpl implements Serializable, VectorLayersDao {
	private static final Logger LOGGER = Logger.getLogger(RemoteVectorLayersDaoImpl.class.getName());

	/**
	 * Constructs a data object access to retrieve data from the remote server.
	 * 
	 * @param url
	 *            webservice baseUrl
	 * @param defaultType
	 *            type name of the seabed habitat
	 */
	public RemoteVectorLayersDaoImpl() {}

	/**
	 * Fetches data from remote server and returns a {@link FeatureCollection}.
	 * 
	 * @param bbox
	 *            bounding box
	 * @param type
	 *            the layer type
	 * @return {@link FeatureCollection}
	 */
	@Override
	public FeatureCollection getFeatures(Rectangle bbox, FeatureType type) {
		try {
			LOGGER.fine("Making a call to get"+bbox.getCoordinates());
			FeatureCollection fc;
			String url = URIUtil.addPaths(type.getLayer().getUrl(), "request=GetFeature");
			if (url.contains("outputFormat=application/json") || url.endsWith("json")) {
				fc = fetchJSON(bbox, type);
			} else {
				fc = fetchXML(bbox, type);
			}
			fc = fc.clippedWith(bbox);
			return fc;
			
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * Gets the data from the WFS service and process it as XML.
	 * 
	 * @param bbox
	 *            bounding box
	 * @param type
	 *            layer typeName
	 * @return {@link FeatureCollection}
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private FeatureCollection fetchXML(Rectangle bbox, FeatureType type)
			throws SAXException, IOException, ParserConfigurationException {
		LOGGER.log(Level.FINE, "Querying WMS server");
		String URL = getFormattedURL(bbox, type, "GetFeature");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		FeatureSaxHandler userhandler = new FeatureSaxHandler();
		saxParser.parse(Util.fetchFrom(URL), userhandler);
		LOGGER.log(Level.FINE, "Got result from" + URL);
		FeatureCollection fc = userhandler.getFeatures();
		return fc;
	}

	/**
	 * Gets data from WFS service and process it as json.
	 * 
	 * @param bbox
	 *            bounding box
	 * @param type
	 *            layer typeName
	 * @return {@link FeatureCollection}
	 * @throws IOException
	 */
	private FeatureCollection fetchJSON(Rectangle bbox, FeatureType type) {
		String URL = getFormattedURL(bbox, type, "GetFeature");
		try (InputStream in = Util.fetchFrom(URL)) {
//			ByteArrayOutputStream result = new ByteArrayOutputStream();
//			byte[] buffer = new byte[1024];
//			int length;
//			while ((length = in.read(buffer)) != -1) {
//				result.write(buffer, 0, length);
//			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return new FeatureCollectionBuilder(new InputStreamReader(in, StandardCharsets.UTF_8.name())).create();
		} catch (IOException e) {
			throw new FatalException(e);
		}

	}

	private String getFormattedURL(Rectangle bbox, FeatureType type, String requestType) {
		Map<String,String> props = new HashMap<String, String>() {{
			put("bbox", Util.rectangleToBBoxString(bbox));
			put(Layer.PARAM_TYPE, type.getName());
			put(Layer.PARAM_REQ_TYPE, requestType);
		}};
		return type.getLayer().resolveUrl(props);

	}

}
