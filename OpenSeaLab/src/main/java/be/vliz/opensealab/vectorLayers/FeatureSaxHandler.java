package be.vliz.opensealab.vectorLayers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import be.vliz.opensealab.feature.Feature;
import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.GeometryFactory;
import be.vliz.opensealab.feature.Polygon;

public class FeatureSaxHandler extends DefaultHandler {
	private final FeatureCollection featureCollection = new FeatureCollection();
	private Feature feature;

	private boolean isFeature;
	private boolean multi;
	private boolean polygon;

	private String element;
	private StringBuilder sb;
	private Polygon pol;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		sb = new StringBuilder();
		if (qName.equals("gml:featureMember") || qName.equals("wfs:member")) {
			feature = new Feature();
			featureCollection.addFeature(feature);
			multi = false;
			isFeature = true;
			return;
		}
		element = qName.split(":")[1];
		if (element.equals("MultiSurface")) { // add more conditions if needed (e.g multilinestring)
			multi = true;
			return;
		}
		if (element.equals("Polygon")) {
			pol = new Polygon();
			polygon = true;
			return;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String endElement = qName.split(":")[1];
		if (endElement == null) {
			return;
		}
		if (polygon && (endElement.equals("exterior") || endElement.equals("interior"))) {
			pol.addRing(GeometryFactory.createPoints(sb.toString()));
			return;
		}
		if (endElement.equals("Polygon")) {
			if (multi) {
				Polygon geo = (Polygon) feature.getGeometry();
				if (geo == null) {
					geo = new Polygon();
					feature.setGeometry(geo);
				}
				geo.addRings(pol);
			} else {
				feature.setGeometry(pol);
			}

			polygon = false;
			return;
		}
		if (endElement.equals("Point")) {
			if (multi) {
				// TODO deal with multipoint
			} else {
				String s = sb.toString();
				if (s.contains(",")) {
					String[] splited = s.split(",");
					s = splited[1] + " " + splited[0];
				}
				feature.setGeometry(GeometryFactory.newPoint(s));
			}
			return;
		}

		if (endElement.equals("LineString")) {
			if (multi) {
				// TODO deal with multilinestring
			} else {
				feature.setGeometry(GeometryFactory.newLineString(sb.toString()));
			}
			return;
		}

		if (endElement.equals("lowerCorner")) {
			if (isFeature) {
				feature.setBboxLower(GeometryFactory.newPoint(sb.toString()));
			}
			return;
		}
		if (endElement.equals("upperCorner")) {
			if (isFeature) {
				feature.setBboxUpper(GeometryFactory.newPoint(sb.toString()));
			}
			return;
		}

		if (isFeature) {
			feature.addProperty(element, sb.toString());
		}

		element = "";
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (new String(ch, start, length).trim().length() == 0)
			return;
		String s = new String(ch, start, length).replace("\t", " ");
		sb.append(s);
		return;
	}

	public FeatureCollection getFeatures() {
		return featureCollection;
	}

}
