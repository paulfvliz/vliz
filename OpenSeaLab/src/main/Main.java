package main;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import seabedhabitat.SeabedHabitatDAO;
import seabedhabitat.SeabedHabitatServlet;
import seabedhabitat.UCCSeabedHabitat;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		try {
			LOGGER.info("Loading app configuration...");
			AppContext appContext = new AppContext();
			appContext.configLogger("log.properties");
			appContext.loadProperties(args.length == 0 ? "prod.properties" : args[0]);
			SeabedHabitatDAO seabedHabitatDAO = new SeabedHabitatDAO(appContext.getProperty("seabedURL"),
					appContext.getProperty("cache-dir"), appContext.getProperty("seabed-data"),
					appContext.getProperty("seabed-stat"));
			UCCSeabedHabitat uccSeabedHabitat = new UCCSeabedHabitat(seabedHabitatDAO);
			startServer(Integer.parseInt(appContext.getProperty("port")), uccSeabedHabitat);
		} catch (Exception exc) {
			LOGGER.log(Level.SEVERE, "App configuration failed !", exc);
		}
	}

	private static void startServer(int port, UCCSeabedHabitat uccSeabedHabit) throws Exception {
		LOGGER.info("Starting the server...");
		Server server = new Server(port);
		WebAppContext context = new WebAppContext();

		context.setResourceBase("www");

		HttpServlet seabedServlet = new SeabedHabitatServlet(uccSeabedHabit);

		context.addServlet(new ServletHolder(new DefaultServlet()), "/");
		context.addServlet(new ServletHolder(seabedServlet), "/seabed");

		server.setHandler(context);
		server.start();
		LOGGER.info("The server is listening...");
	}

}
