package be.vliz.opensealab.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import be.vliz.opensealab.exceptions.BizzException;
import be.vliz.opensealab.feature.FeatureCollection;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.feature.Square;
import be.vliz.opensealab.feature.SurfaceCount;
import be.vliz.opensealab.vectorLayers.dao.VectorLayersDao;
import be.vliz.opensealab.vectorLayers.model.DataCacheKey;
import be.vliz.opensealab.vectorLayers.model.FeatureType;
import org.ehcache.Cache;
import org.ehcache.CacheManager;

import javax.inject.Inject;

public class PiecedCachingManager implements LayerProvider {
	private static final Logger LOGGER = Logger.getLogger(PiecedCachingManager.class.getName());
	private final VectorLayersDao nonCacheProvider;
	private final Cache<DataCacheKey, FeatureCollection> dataCache;
	private final Cache<DataCacheKey, SurfaceCount> statsCache;

	private AtomicInteger squaresDone = new AtomicInteger(0);

	@Inject
	public PiecedCachingManager(VectorLayersDao nonCacheProvider, CacheManager cacheManager) {
		this.nonCacheProvider = nonCacheProvider;
		dataCache = cacheManager.getCache("featureData", DataCacheKey.class, FeatureCollection.class);
		statsCache = cacheManager.getCache("featureStats", DataCacheKey.class, SurfaceCount.class);

	}

	/**
	 * This method will attempt to load as much data from the cache. Missing pieces
	 * will be retrieved from the 'nonCacheProvider' (thus the WMS-server) and be
	 * cached as well. To optimize caching, data is retrieved as squares of one by
	 * one degree and are saved.
	 * 
	 * @param bbox
	 * @param type
	 * @param dividingProperty
	 *            be.vliz.opensealab.vectorLayers identifier (categorie)
	 * @return
	 */
	public FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, boolean onlyUseCache,
			String geomType) {

		Rectangle extended = bbox.extendRectangle();
		FeatureCollection total = new FeatureCollection();
		for (int lat = (int) extended.getMinLat(); lat < extended.getMaxLat(); lat++) {
			for (int lon = (int) extended.getMinLon(); lon < extended.getMaxLon(); lon++) {
				FeatureCollection found = loadAndCachePart(lat, lon, type, dividingProperty, onlyUseCache, geomType);
				if (bbox.edgePoint(lat, lon) && found != null) {
					found = found.clippedWith(bbox);
				}
				total = total.joinWith(found);
			}
		}
		return total;
	}

	/**
	 * Delegates the hard work to {@link #retrieve(Rectangle, FeatureType, String, boolean, String)}
	 * method with the argument onlyUseCache equals to false.
	 * 
	 * @param bbox
	 * @param type
	 * @param dividingProperty
	 * @return
	 */
	public FeatureCollection retrieve(Rectangle bbox, FeatureType type, String dividingProperty, String geomType) {
		return retrieve(bbox, type, dividingProperty, false, geomType);
	}

	public SurfaceCount retrieveStats(Rectangle bbox, FeatureType type, String dividingProperty, String geomType) {
		Rectangle extended = bbox.extendRectangle();
		SurfaceCount sc = new SurfaceCount();

		for (int lat = (int) extended.getMinLat(); lat < extended.getMaxLat(); lat++) {
			for (int lon = (int) extended.getMinLon(); lon < extended.getMaxLon(); lon++) {

				Rectangle searched = new Rectangle(lat, lon, lat + 1, lon + 1);

				if (bbox.edgePoint(lat, lon)) {
					// we can't load the statistic of cache as this is an edgepoint
					FeatureCollection found = loadAndCachePart(lat, lon, type, dividingProperty, false, geomType);
					sc = sc.merge(found.clippedWith(bbox).calculateTotals(dividingProperty));
					continue;
				}

				DataCacheKey cacheKey = new DataCacheKey(type, searched);
				if (statsCache.containsKey(cacheKey)) {
					// statistics are in cache!
					SurfaceCount found = statsCache.get(cacheKey);
					sc = sc.merge(found);
					continue;
				}

				// statistics are not in the cache yet
				FeatureCollection found = loadAndCachePart(lat, lon, type, dividingProperty, false, geomType);
				sc = sc.merge(found.calculateTotals(dividingProperty));
			}
		}
		return sc;
	}

	/**
	 * This method is made to give a huge bbox (such as the entire coverage of the
	 * layer), which will then call the remote WFS server once. It will get a ton of
	 * data back, which it'll start chopping up in pieces of one latitude*one
	 * longitude.
	 * 
	 * (Note: it will start to check if the cache is empty for this layer+type. If
	 * not, the method will not do anything)
	 * 
	 * @param bbox
	 * @param type
	 * @param dividingProperty
	 */
	public void loadAndCacheAll(Rectangle bbox, FeatureType type, String dividingProperty, Runnable whenDone) {
		bbox = bbox.extendRectangle();
		DataCacheKey cacheKey = new DataCacheKey(type, new Square(bbox.getMinLat(), bbox.getMinLon()));
		if (dataCache.containsKey(cacheKey)) {
			// already cached! Abort
			LOGGER.info("Already cached: " + type);
			whenDone.run();
			return;
		}

		LOGGER.info("Computer freeze incoming...");
		final FeatureCollection fromServer = nonCacheProvider.getFeatures(bbox, type);
		LOGGER.info("Everything is downloaded and parsed. Start of clipping + caching...");

		int squaresGoal = (int) ((bbox.getMaxLat() - bbox.getMinLat()) * (bbox.getMaxLon() - bbox.getMinLon()));
		int squaresFormat = ("" + squaresGoal).length();

		ExecutorService threads = Executors.newFixedThreadPool(3);

		System.out.printf("%" + squaresFormat + "d/%d", squaresDone, squaresGoal);
		for (int lat = (int) bbox.getMinLat(); lat < bbox.getMaxLat(); lat++) {
			for (int lon = (int) bbox.getMinLon(); lon < bbox.getMaxLon(); lon++) {
				final Square s = new Square(lat, lon);
				DataCacheKey key = new DataCacheKey(type, s);
				Runnable task = () -> {
					FeatureCollection toCache = fromServer.clippedWith(s);
					dataCache.put(key, toCache);
					statsCache.put(key, toCache.calculateTotals(dividingProperty));

					squaresDone.incrementAndGet();
					System.out.printf("\r%" + squaresFormat + "d/%d", squaresDone, squaresGoal);
					if (squaresDone.get() == squaresGoal && whenDone != null) {
						LOGGER.info("All done with layer " + type);
						new Thread(whenDone).start();
					}
				};
				threads.execute(task);
			}
		}
		threads.shutdown();

	}

	private FeatureCollection loadAndCachePart(int lat, int lon, FeatureType type, String dividingProperty,
			boolean onlyUseCache, String geomType) {
		DataCacheKey key = new DataCacheKey(type, new Square(lat, lon));
		FeatureCollection found = null;
		if (dataCache.containsKey(key)) {
			found = dataCache.get(key);
		}

		if (found == null && !onlyUseCache) {
			// caching file might have gotten corrupted and might have returned null
			found = nonCacheProvider.getFeatures(key.getGrid(), type);
			dataCache.put(key, found);
			SurfaceCount stats = null;
			if (geomType.equals("polygon") || geomType.equals("point")) {
				stats = found.calculateTotals(dividingProperty);
				statsCache.put(key, stats);
			} else if (geomType.equals("line")) {
				// TODO call to statistics method
			} else {
				throw new BizzException("Unknown geomType.");
			}
		}
		return found;
	}

}
