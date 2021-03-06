package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import be.vliz.emodnet.querytool.core.common.Util;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriTemplate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Layer implements Serializable{
	private static final long serialVersionUID = 168489331194062745L;

	public static final String PARAM_TYPE = "type";
	public static final String PARAM_REQ_TYPE = "requestType";
	public static final String PARAM_FILTER = "filter";

	private static final String DEFAULT_REQUEST_TYPE = "getFeatures";

	private final String name;
	private final String url;
	private final String defaultType;
	private final String defaultDividor;
	private final Rectangle bbox;
	private final Map<String,String> defaultParams;


	public Layer(String name, String url, String defaultType, String defaultDividor, Rectangle bbox) {
		this.name = name;
		this.url = url;
		this.defaultType = defaultType;
		this.defaultDividor = defaultDividor;
		this.bbox = bbox;

		this.defaultParams = new HashMap<String, String>() {
			private static final long serialVersionUID = 168489331194062795L;

			{
			put(PARAM_TYPE, defaultType);
			put(PARAM_REQ_TYPE, DEFAULT_REQUEST_TYPE);
			put(PARAM_FILTER, "1=1");
			put("bbox", Util.rectangleToBBoxString(bbox));
			put("cql_bbox", Util.rectangleToBBoxString(bbox, true));
		}};
	}

	public String getName() {
		return name;
	}

	public String resolveUrl(Map<String, String> parameters) {
		Map<String,String> params = new HashMap<>(this.defaultParams);
		params.putAll(parameters);
		String url = UriTemplate.resolveTemplateValues(UriComponent.Type.QUERY, this.url, true, params);
		url = url.replaceAll("([^&?]*)=(?:&|$)",""); // remove empty parameters
		return url;
	}

	public String getUrl() {
		return url;
	}

	public String getDefaultType() {
		return defaultType;
	}

	public String getDefaultDividor() {
		return defaultDividor;
	}

	public Rectangle getBbox() {
		return bbox;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Layer layer = (Layer) o;
		return name.equals(layer.name) &&
				url.equals(layer.url) &&
				Objects.equals(defaultType, layer.defaultType) &&
				Objects.equals(defaultDividor, layer.defaultDividor) &&
				Objects.equals(bbox, layer.bbox) &&
				Objects.equals(defaultParams, layer.defaultParams);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, url, defaultType, defaultDividor, bbox, defaultParams);
	}
}
