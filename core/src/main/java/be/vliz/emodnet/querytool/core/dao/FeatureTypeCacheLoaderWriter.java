package be.vliz.emodnet.querytool.core.dao;

import org.ehcache.spi.loaderwriter.CacheLoaderWriter;

import java.util.LinkedList;

public class FeatureTypeCacheLoaderWriter implements CacheLoaderWriter<String, LinkedList> {
    private LayerDao layerDao;
    private FeatureTypeDao featureTypeDao;

    public FeatureTypeCacheLoaderWriter(LayerDao layerDao, FeatureTypeDao featureTypeDao) {
        this.layerDao = layerDao;
        this.featureTypeDao = featureTypeDao;
    }

    @Override
    public LinkedList load(String key) throws Exception {
        return new LinkedList<>(
                featureTypeDao.getFeatureTypes(
                        layerDao.getLayer(key)
                )
        );
    }

    @Override
    public void write(String key, LinkedList value) throws Exception {
        throw new RuntimeException();
    }

    @Override
    public void delete(String key) throws Exception {
        throw new RuntimeException();
    }
}
