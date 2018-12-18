package be.vliz.emodnet.querytool.vectorLayers.model;

import be.vliz.emodnet.querytool.feature.Rectangle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FeatureType implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Layer layer;
	private String name;
	private String title;
	private String abstractText;
	private List<String> keywords;
	private String defaultSrs;
	private Rectangle bbox;

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
		return keywords != null ? Collections.unmodifiableList(keywords) : null;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
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
				Objects.equals(bbox, that.bbox);
	}

	@Override
	public int hashCode() {
		return Objects.hash(layer, name, title, abstractText, keywords, defaultSrs, bbox);
	}
}
