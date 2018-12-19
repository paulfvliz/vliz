package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Rectangle;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DataCacheKeyTest {

    @Test
    public void testEquals() {
        Layer layer = new Layer("test", "http://123.here", null, null, new Rectangle(0,0,1,1));
        Layer layer2 = new Layer("test", "http://123.here", null, null, new Rectangle(0,0,1,1));
        FeatureType ft = new FeatureType(layer, "test-ft", null, null);
        FeatureType ft2 = new FeatureType(layer2, "test-ft", null, null);
        DataCacheKey key1 = new DataCacheKey(ft, new Rectangle(0,0,1,1));
        DataCacheKey key2 = new DataCacheKey(ft2, new Rectangle(0,0,1,1));

        assertThat(key1, equalTo(key2));
    }
}
