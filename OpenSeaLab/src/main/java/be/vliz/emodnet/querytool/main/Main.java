package be.vliz.emodnet.querytool.main;

import be.vliz.emodnet.querytool.bathymetry.BathymetryDAO;
import be.vliz.emodnet.querytool.bathymetry.BathymetryServlet;
import be.vliz.emodnet.querytool.bathymetry.UCCBathymetry;
import be.vliz.emodnet.querytool.vectorLayers.dao.LayerDao;
import be.vliz.emodnet.querytool.vectorLayers.dao.PropertiesLayerDaoImpl;
import be.vliz.emodnet.querytool.vectorLayers.dao.RemoteVectorLayersDaoImpl;
import be.vliz.emodnet.querytool.vectorLayers.dao.VectorLayersDao;
import be.vliz.emodnet.querytool.vectorLayers.model.FeatureType;
import be.vliz.emodnet.querytool.vectorLayers.model.Layer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.annotation.PreDestroy;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");


		Server server = new Server(Integer.parseInt(appContext.getProperty(("port"))));
		server.setHandler(context);
		//WebAppContext context = new WebAppContext();

		context.setResourceBase(Main.class.getResource("/www").toString());

		ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/eqt/*");
		servletHolder.setInitOrder(0);
		servletHolder.setInitParameter("javax.ws.rs.Application", App.class.getCanonicalName());
		servletHolder.setInitParameter(
				"jersey.config.server.provider.packages",
				"be.vliz.emodnet.querytool.vectorLayers.controller;io.swagger.v3.jaxrs2.integration.resources");



		BathymetryDAO bathymetryDAO = new BathymetryDAO(appContext.getProperty("bathymetry"),
				appContext.getProperty("cache-dir"), appContext.getProperty("bathymetry-stat"));
		UCCBathymetry uccBathymetry = new UCCBathymetry(bathymetryDAO);

		HttpServlet bathymetryServlet = new BathymetryServlet(uccBathymetry);
		context.addServlet(new ServletHolder(bathymetryServlet), "/bathymetry");

		FilterHolder crossOriginFilter = new FilterHolder(new CrossOriginFilter());
		crossOriginFilter.setInitParameters(appContext.getPropertyNames().stream()
			.filter(p -> p.startsWith("cors."))
			.collect(Collectors.toMap(p -> p.substring(5), p ->  appContext.getProperty(p) )));
		context.addFilter(crossOriginFilter, "/*", EnumSet.allOf(DispatcherType.class));

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Shutdown hook!");
				LOGGER.info("Stopping!");
				try {
					server.stop();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		});

		try {
            server.start();
            LOGGER.info("The server is listening...");
            server.join();
        } catch (Throwable e) {
		    LOGGER.log(Level.SEVERE, "Could not start server", e);
		} finally {
			LOGGER.info("Stopping!");
			server.destroy();
		}

	}

	private static Runnable populateCache(final AppContext appContext, final String layerName, final Runnable whenDone) throws Exception {
		return new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("Running cache population for "+layerName);
					LayerDao layerDao = new PropertiesLayerDaoImpl(appContext.getProperties());

					XmlConfiguration cacheConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
					CacheManager cacheManager = CacheManagerBuilder.newCacheManager(cacheConfig);
					cacheManager.init();

					populateCache(appContext, layerDao.getLayer(layerName), cacheManager, whenDone);
				} catch (Exception e) {
					e.printStackTrace();
					whenDone.run();
				}
			}
			
		};

	}

	private static void populateCache(AppContext appContext, Layer layer, CacheManager cacheManager, Runnable whenDone)
			throws Exception {
		PiecedCachingManager pcm = createPCM(appContext.getProperty("cache-dir"), cacheManager);
		pcm.loadAndCacheAll(layer.getBbox(), new FeatureType(layer, layer.getDefaultType(), null, null), layer.getDefaultDividor(), whenDone);
	}

	private static PiecedCachingManager createPCM(String cacheDir, CacheManager cacheManager) throws Exception {
		VectorLayersDao vectorLayersDAO = new RemoteVectorLayersDaoImpl();

		PiecedCachingManager pcm = new PiecedCachingManager(vectorLayersDAO, cacheManager);
		return pcm;
	}

	@PreDestroy
	public void cleanup() {
		System.out.println("THIS IS THE END OF MAIN");
	}

}