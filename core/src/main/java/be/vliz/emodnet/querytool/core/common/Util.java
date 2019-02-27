package be.vliz.emodnet.querytool.core.common;

import be.vliz.emodnet.querytool.core.exceptions.FatalException;
import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import com.google.common.base.Joiner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
	private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
	private static int READ_TIMEOUT = (int)Duration.ofMinutes(5).toMillis();

	public static void store(String data, Path p, String directory) {
		try {
			Path cache = FileSystems.getDefault().getPath(normalizePath(directory));
			if (!Files.exists(cache) || !Files.isDirectory(cache)) {
				Files.createDirectory(cache);
				LOGGER.log(Level.FINE, "Caching directory created ");
			}
			try (Writer writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
				writer.write(data);
				LOGGER.log(Level.FINE, "Cache file " + p + " created");
			}
		} catch (IOException io) {
			throw new FatalException("The file cannot be saved", io);
		}
	}

	public static String normalizePath(String path) {
		return path.replaceAll(":", "");
	}

	public static InputStream fetchFrom(String url) {

		if (url.startsWith("file://")) {
			Path p = Paths.get(url.substring("file://".length()));
			try {
				return Files.newInputStream(p);
			} catch (IOException e) {
				throw new FatalException(e);
			}
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setReadTimeout(READ_TIMEOUT);
			connection.setConnectTimeout(READ_TIMEOUT);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			return connection.getInputStream();
		} catch (IOException e) {
			throw new FatalException(e);
		}

	}

	public static Rectangle getBBox(HttpServletRequest req) {
		return new Rectangle(req.getParameter("latmin"), req.getParameter("lonmin"), req.getParameter("latmax"),
				req.getParameter("lonmax"));
	}

	private static final String DEFAULT_PROJECTION = "urn:ogc:def:crs:EPSG::4326";
	public static String rectangleToBBoxString(Rectangle bbox) {
		return rectangleToBBoxString(bbox, false);
	}

	public static String rectangleToBBoxString(Rectangle bbox, boolean excludeProjection) {
	  String[] a = new String[]{
	    Double.toString(bbox.getMinLat()),
      Double.toString(bbox.getMinLon()),
      Double.toString(bbox.getMaxLat()),
      Double.toString(bbox.getMaxLon()),
      DEFAULT_PROJECTION};
	  if (excludeProjection) {
	    a = Arrays.copyOf(a, 4);
    }

    return Joiner.on(",").join(a);
  }
}
