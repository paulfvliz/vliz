package be.vliz.emodnet.querytool.server;

import be.vliz.emodnet.querytool.core.dao.*;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends ResourceConfig {
    private CacheManager cacheManager;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static String getEnv(String property, Context ctx, String defaultValue) {
        try {
            return (String)ctx.lookup("java:comp/env/" + property);
        } catch (NamingException e) {
            // No entry found fall back to default
        }
        return defaultValue;
    }

    public App() {
        LOGGER.fine("Creating EMODNet Query Tool App context");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                try {
                    Context ctx = new InitialContext();
                    URL cacheConfigUrl = getClass().getResource("/ehcache.xml");
                    try {
                        cacheConfigUrl = new URL((String)ctx.lookup("java:comp/env/cacheConfig"));
                    } catch (NamingException e) {
                        // No entry found fall back to default
                    }

                    AppContext appContext = new AppContext();
                    appContext.loadProperties(getEnv("appConfig", ctx, "prod.properties"));

                    LayerDao layerDao = new PropertiesLayerDaoImpl(appContext.getProperties());
                    FeatureTypeDao remoteFeatureTypeDao = new RemoteFeatureTypeDaoImpl();

                    bind(layerDao).to(LayerDao.class);
                    bind(CachingFeatureTypeDaoImpl.class).to(FeatureTypeDao.class).in(Singleton.class);
                    bind(PiecedCachingManager.class).to(LayerProvider.class);
                    bind(RemoteVectorLayersDaoImpl.class).to(VectorLayersDao.class);
                    bind(appContext.getProperties()).named("app-properties");
                    bind(remoteFeatureTypeDao).to(FeatureTypeDao.class).named("remotedao");


                    XmlConfiguration cacheConfig = new XmlConfiguration(cacheConfigUrl);
                    cacheManager = CacheManagerBuilder.newCacheManager(cacheConfig);
                    cacheManager.init();

                    // creating cache here since this users a loader-writer which is hard to define in the xml config
                    cacheManager.createCache("featureType",
                       cacheConfig.newCacheConfigurationBuilderFromTemplate(
                                "featureTypeTemplate", String.class, LinkedList.class)
                        .withLoaderWriter(
                            new FeatureTypeCacheLoaderWriter(layerDao, remoteFeatureTypeDao)
                        ).build());

                    bind(cacheManager).to(CacheManager.class);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to initialize webapp", e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        LOGGER.fine("Cleaning up resources.");
        if (this.cacheManager != null) {
            this.cacheManager.close();
        }
    }
}
