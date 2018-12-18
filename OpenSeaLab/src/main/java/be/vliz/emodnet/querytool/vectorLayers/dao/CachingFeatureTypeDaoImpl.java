package be.vliz.emodnet.querytool.vectorLayers.dao;

import be.vliz.emodnet.querytool.exceptions.BizzException;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;
import be.vliz.emodnet.querytool.vectorLayers.model.Layer;
import org.ehcache.Cache;
import org.ehcache.CacheManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;

public class CachingFeatureTypeDaoImpl extends AbstractFeatureTypeDao {
	private final Cache<String, LinkedList> cache;

	@Inject
	public CachingFeatureTypeDaoImpl(CacheManager cacheManager) {
		this.cache = cacheManager.getCache("featureType", String.class, LinkedList.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<FeatureType> getFeatureTypes(Layer layer) throws BizzException {
		return cache.get(layer.getName());
	}
}
