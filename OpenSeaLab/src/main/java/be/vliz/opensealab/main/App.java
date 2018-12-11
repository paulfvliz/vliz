package be.vliz.opensealab.main;

import be.vliz.opensealab.vectorLayers.dao.RemoteVectorLayersDaoImpl;
import be.vliz.opensealab.vectorLayers.dao.VectorLayersDao;
import be.vliz.opensealab.vectorLayers.dao.*;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends ResourceConfig {
    private CacheManager cacheManager;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());



    public App() {
        LOGGER.fine("Creating OSL app context");
        register(new AbstractBinder() {
            @Override
            protected void configure() {

                AppContext appContext = new AppContext();
                appContext.loadProperties("prod.properties");

                LayerDao layerDao = new PropertiesLayerDaoImpl(appContext.getProperties());

                bind(layerDao).to(LayerDao.class);
                bind(CachingFeatureTypeDaoImpl.class).to(FeatureTypeDao.class).in(Singleton.class);
                bind(PiecedCachingManager.class).to(LayerProvider.class);
                bind(RemoteVectorLayersDaoImpl.class).to(VectorLayersDao.class);
                bind(appContext.getProperties()).named("app-properties");

                try {
                    XmlConfiguration cacheConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
                    FeatureTypeDao remoteFeatureTypeDao = new RemoteFeatureTypeDaoImpl();
                    bind(remoteFeatureTypeDao).to(FeatureTypeDao.class).named("remotedao");

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
