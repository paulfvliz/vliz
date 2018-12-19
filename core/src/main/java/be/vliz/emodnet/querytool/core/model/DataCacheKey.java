package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Rectangle;

import java.io.Serializable;
import java.util.Objects;

public class DataCacheKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FeatureType featureType;
    private final Rectangle grid;

    public DataCacheKey(FeatureType featureType, Rectangle grid) {
        this.featureType = featureType;
        this.grid = grid;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public Rectangle getGrid() {
        return grid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataCacheKey cacheKey = (DataCacheKey) o;
        return featureType.equals(cacheKey.featureType) &&
                grid.equals(cacheKey.grid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureType, grid);
    }
}
