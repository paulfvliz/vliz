package be.vliz.opensealab.main;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.vliz.opensealab.exceptions.FatalException;
import be.vliz.opensealab.feature.Rectangle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingManager implements Serializable {
	private static final long serialVersionUID = 4132525243761526794L;
	private static final Logger LOGGER = Logger.getLogger(CachingManager.class.getName());
	private final String cache;
	private final String pattern;
	private final String layerName;
	private final LoadingCache<Path, Object> memCache = CacheBuilder.newBuilder().maximumSize(10000)
		.build(new CacheLoader<Path, Object>() {
			@Override
			public Object load(Path p) throws Exception {
				return CachingManager.restoreInternal(p);
			}
		});

	public CachingManager(String layerName, String cache, String pattern) throws IOException {
		this.cache = cache + "/" + layerName;
		this.pattern = pattern;
		this.layerName = layerName;
	}

	/**
	 * Stores as file the argument data, the name will be a combination of the
	 * bounding box and the type.
	 * 
	 * @param data
	 *            the data to store
	 * @param bbox
	 *            the bounding box
	 * @param type
	 *            the type of the layer
	 */
	public void store(String data, Rectangle bbox, String type) {
		Path p = getPath(bbox, type);
		initCacheDir(p);
		try (Writer writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
			Path parentPath = p.getParent();
			if (parentPath != null && Files.notExists(parentPath)) {
				Files.createDirectories(parentPath);
			}
			writer.write(data);
			LOGGER.log(Level.FINE, "Cache file " + p + " created");
		} catch (IOException io) {
			throw new FatalException("The file cannot be saved", io);
		}
	}

	/**
	 * Stores as file the argument ser, the name will be a combination of the
	 * bounding box and the type.
	 * 
	 * @param ser
	 *            a serializable object
	 * @param bbox
	 *            the bounding box
	 * @param type
	 *            the type of the layer
	 */
	public void store(Serializable ser, Rectangle bbox, String type) {
		Path p = getPath(bbox, type);
		initCacheDir(p);
		try (ObjectOutputStream fileOut = new ObjectOutputStream(Files.newOutputStream(p))) {
			fileOut.writeObject(ser);
			fileOut.flush();
		} catch (IOException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * Retrieves a stored object.
	 * 
	 * @param bbox
	 * @param type
	 * @return
	 */
	public <T> T restore(Rectangle bbox, String type) {
		Path p = getPath(bbox, type);
		try {
			return (T) memCache.get(p);
		} catch (ExecutionException e) {
			LOGGER.log(Level.WARNING,"Could not get cache for " + p, e);
			return null;
		}
	}

	private static <T> T restoreInternal(Path path) {
		try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
			LOGGER.fine("Cache hit for "+path);
			return (T) in.readObject();
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Could not load " + path + ", purging it from cache", e);
			String pathString = path.toString();
			if (!(new File(pathString).delete())){
				LOGGER.warning("Could not delete path " + path);
			}
			return null;
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not restore cache.", e);
			return null;
		}
	}

	/**
	 * Stores as file the argument data, the name will be a composed of the bounding
	 * box.
	 * 
	 * @param data
	 * @param bbox
	 */
	public void store(String data, Rectangle bbox) {
		store(data, bbox, null);
	}

	/**
	 * Returns a path object. The pathname will be a combination of the previously
	 * specified cache directory and the parameters of this method.
	 * 
	 * @param bbox
	 * @param type
	 * @return a {@link Path}
	 */
	public Path getPath(Rectangle bbox, String type) {
		return FileSystems.getDefault().getPath(Util.normalizePath(cache + "/" + pattern.replace("{id}", getId(bbox, type))));
	}

	private String getId(Rectangle bbox, String type) {
		if (type == null) {
			return layerName + "_" + bbox.getMinLat() + "_" + bbox.getMinLon() + "_" + bbox.getMaxLat() + "_"
					+ bbox.getMaxLon();
		} else {
			return type + "/" + type + "_" + bbox.getMinLat() + "_" + bbox.getMinLon() + "_" + bbox.getMaxLat() + "_"
					+ bbox.getMaxLon();

		}
	}

	/**
	 * Checks whether a file exists with this method parameters in its name
	 * (following the specified pattern).
	 * 
	 * @param bbox
	 * @param type
	 * @return true if file found, false if not
	 */
	public boolean isInCache(Rectangle bbox, String type) {
		return Files.exists(getPath(bbox, type));
	}

	private static void initCacheDir(Path p) {
		Path parentPath = p.getParent();
		if (parentPath != null && Files.notExists(parentPath)) {
			try {
				Files.createDirectories(parentPath);
			} catch (IOException e) {
				throw new FatalException(e);
			}
		}
	}

}
