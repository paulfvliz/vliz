package be.vliz.emodnet.querytool.core.dao;

import be.vliz.emodnet.querytool.core.exceptions.BizzException;
import be.vliz.emodnet.querytool.core.model.FeatureType;
import be.vliz.emodnet.querytool.core.model.Layer;
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
