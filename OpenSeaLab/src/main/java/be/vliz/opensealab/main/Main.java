package be.vliz.opensealab.main;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.webapp.WebAppContext;

import be.vliz.opensealab.bathymetry.BathymetryDAO;
import be.vliz.opensealab.bathymetry.BathymetryServlet;
import be.vliz.opensealab.bathymetry.UCCBathymetry;
import be.vliz.opensealab.feature.Rectangle;
import be.vliz.opensealab.vectorLayers.VectorLayersDAO;
import be.vliz.opensealab.vectorLayers.VectorLayersServlet;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		System.out.println("VLIZ Server 1.0");
		try {
			LOGGER.info("Loading app configuration...");
			AppContext appContext = new AppContext();
			AppContext.configLogger("log.properties");
	
			
			if(args.length != 0 && args[0].equals("--populate-cache")) {
				appContext.loadProperties("prod.properties");
				
				List<String> knownLayers =
						Arrays.asList(appContext.getProperty("known-layers").split(";"));
				
				
				Runnable lastAction = () -> {
						System.out.println("All done, exiting now");
				};
				
				for (String layer : knownLayers) {
					lastAction = populateCache(appContext, layer, lastAction);
				}
				lastAction.run();
				
				return;
			}
			appContext.loadProperties(args.length == 0 ? "prod.properties" : args[0]);

			startServer(appContext);
		} catch (Exception exc) {
			LOGGER.log(Level.SEVERE, "App configuration failed !", exc);
		}
	}

	private static void startServer(AppContext appContext) throws Exception {
		LOGGER.info("Starting the server...");
		Server server = new Server(Integer.parseInt(appContext.getProperty(("port"))));
		WebAppContext context = new WebAppContext();

		BathymetryDAO bathymetryDAO = new BathymetryDAO(appContext.getProperty("be/vliz/opensealab/bathymetry"),
				appContext.getProperty("cache-dir"), appContext.getProperty("bathymetry-stat"));
		UCCBathymetry uccBathymetry = new UCCBathymetry(bathymetryDAO);

		context.setResourceBase(Main.class.getResource("/www").toString());
		context.addServlet(new ServletHolder(new DefaultServlet()), "/");

		List<String> knownLayers = 
				Arrays.asList(appContext.getProperty("known-layers").split(";"));
	
		for (String layer : knownLayers) {
			initVectorLayerServlet(layer, appContext, context);
		}
		//initVectorLayerFromFile("test.json", context);
		
		HttpServlet bathymetryServlet = new BathymetryServlet(uccBathymetry);
		context.addServlet(new ServletHolder(bathymetryServlet), "/be/vliz/opensealab/bathymetry");

		FilterHolder crossOriginFilter = new FilterHolder(new CrossOriginFilter());
		crossOriginFilter.setInitParameters(appContext.getPropertyNames().stream()
			.filter(p -> p.startsWith("cors."))
			.collect(Collectors.toMap(p -> p.substring(5), p ->  appContext.getProperty(p) )));
		context.addFilter(crossOriginFilter, "/*", EnumSet.allOf(DispatcherType.class));

		server.setHandler(context);
		server.start();
		LOGGER.info("The server is listening...");
	}

	private static Runnable populateCache(final AppContext appContext, final String layerName, final Runnable whenDone) throws IOException {
		return new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("Running cache population for "+layerName);
					populateCache(appContext, layerName, new Rectangle(appContext, layerName),
							appContext.getProperty(layerName + "-default-type"), appContext.getProperty(layerName + "-default-dividor"), whenDone);
				} catch (IOException e) {
					e.printStackTrace();
					whenDone.run();
				}
			}
			
		};

	}

	private static void populateCache(AppContext appContext, String layerName, Rectangle bbox, String type, String dividor, Runnable whenDone)
			throws IOException {
		PiecedCachingManager pcm = createPCM(layerName, appContext);
		pcm.loadAndCacheAll(bbox, type, dividor, whenDone);
	}
	
//	@SuppressWarnings("unused")
//	private static void initVectorLayerFromFile(String file, WebAppContext context) throws IOException {
//
//		HttpServlet servlet = new VectorLayersServlet(new FromJSONFileLayer("test.json"), "", "");
//		context.addServlet(new ServletHolder(servlet), "/test");
//	}

	private static void initVectorLayerServlet(String layerName, AppContext appContext, WebAppContext context)
			throws IOException {
		String defaultType = appContext.getProperty(layerName + "-default-type");
		String defaultDividor = appContext.getProperty(layerName + "-default-dividor");

		PiecedCachingManager pcm = createPCM(layerName, appContext);
		HttpServlet seabedServlet = new VectorLayersServlet(pcm, defaultType, defaultDividor);

		context.addServlet(new ServletHolder(seabedServlet), "/" + layerName);
	}

	private static PiecedCachingManager createPCM(String layerName, AppContext appContext) throws IOException {
		String defaultType = appContext.getProperty(layerName + "-default-type");

		VectorLayersDAO vectorLayersDAO = new VectorLayersDAO(layerName, appContext.getProperty(layerName),
				defaultType);
		CachingManager dataCache = new CachingManager(layerName, appContext.getProperty("cache-dir"),
				"data-{id}.FeatureCollection");
		CachingManager statsCache = new CachingManager(layerName, appContext.getProperty("cache-dir"),
				"stats-{id}.SurfaceCount");

		PiecedCachingManager pcm = new PiecedCachingManager(vectorLayersDAO, dataCache, statsCache);
		return pcm;
	}

}
