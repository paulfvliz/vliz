package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FeatureType implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_FILTER = "1=1";

	private final Layer layer;
	private String name;
	private String title;
	private String abstractText;
	private ImmutableList<String> keywords;
	private String defaultSrs;
	private Rectangle bbox;
	private ImmutableMap<String,String> properties;
	private String filter = DEFAULT_FILTER;

	public FeatureType(Layer layer,String name, String title, String abstractText) {
		this.layer = layer;
		this.name = name;
		this.title = title;
		this.abstractText = abstractText;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = ImmutableList.copyOf(keywords);
	}

	public String getDefaultSrs() {
		return defaultSrs;
	}

	public void setDefaultSrs(String defaultSrs) {
		this.defaultSrs = defaultSrs;
	}

	public Rectangle getBbox() {
		return bbox;
	}

	public void setBbox(Rectangle bbox) {
		this.bbox = bbox;
	}

	public Layer getLayer() {
		return layer;
	}

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = ImmutableMap.copyOf(properties);
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FeatureType that = (FeatureType) o;
		return layer.equals(that.layer) &&
				name.equals(that.name) &&
				Objects.equals(title, that.title) &&
				Objects.equals(abstractText, that.abstractText) &&
				Objects.equals(keywords, that.keywords) &&
				Objects.equals(defaultSrs, that.defaultSrs) &&
				Objects.equals(bbox, that.bbox) &&
        Objects.equals(filter, that.filter);

	}

	@Override
	public int hashCode() {
		return Objects.hash(layer, name, title, abstractText, keywords, defaultSrs, bbox, filter);
	}
}
