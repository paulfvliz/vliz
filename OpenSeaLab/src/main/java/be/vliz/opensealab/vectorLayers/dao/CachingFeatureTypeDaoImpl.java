package be.vliz.opensealab.vectorLayers.dao;

import be.vliz.opensealab.exceptions.BizzException;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import be.vliz.opensealab.vectorLayers.model.Layer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

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
