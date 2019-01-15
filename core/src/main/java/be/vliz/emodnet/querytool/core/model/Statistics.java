package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Feature;
import be.vliz.emodnet.querytool.core.model.feature.FeatureCollection;
import be.vliz.emodnet.querytool.core.model.feature.Geometry;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistics implements Serializable {
  private static final long serialVersionUID = -8500959422569483025L;
  private final ImmutableMap<String, Statistic> statistics;
  private final String dividingProperty;
  private final Double total;

  private Statistics(String dividingProperty, final Map<String, Statistic.Builder> statistics) {
    this.dividingProperty = dividingProperty;
    this.statistics = statistics.entrySet().stream().collect(ImmutableMap.toImmutableMap(
      e -> e.getKey(),
      e -> e.getValue().build(this)
    ));
    this.total = this.statistics.values().stream().mapToDouble(Statistic::getTotal).sum();
  }

  public Map<String, Statistic> getStatistics() {
    return statistics;
  }

  public double getTotal() {
    return total;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String dividingProperty;
    private Map<String, Statistic.Builder> statistics = new HashMap<>();

    private Builder() {
    }

    public Builder dividingProperty(String dividingProperty) {
      this.dividingProperty = dividingProperty;
      return this;
    }

    public Statistics build() {
      return new Statistics(dividingProperty, statistics);
    }

    public Builder of(Statistics s) {
      dividingProperty(s.dividingProperty);
      statistics = new HashMap<>(s.statistics.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> Statistic.builder().of(e.getValue()))));
      return this;
    }

    public Builder add(FeatureCollection fc) {
      for (Feature f : fc.getFeatures()) {
        this.add(f);
      }
      return this;
    }

    public Builder add(Feature f) {
      Geometry geo = f.getGeometry();
      if (geo == null)
        return this;

      Map<String, Object> m = f.getProperties();
      String name = (String) m.get(dividingProperty); // used "AllcombD" previously
      Statistic.Builder s = statistics.getOrDefault(name, Statistic.builder().name(name));
      s.add(f);

      statistics.putIfAbsent(name, s);
      return this;
    }

    public Builder add(Statistic s) {

      return this;
    }

    public Builder add(Statistics toMerge) {
      for (Statistic s : toMerge.statistics.values()) {
        Statistic.Builder sb = Statistic.builder().of(s);
        this.statistics.merge(
          s.getName(),
          sb,
          (oldValue, value) -> oldValue.add(value.build(null)));
      }
      return this;
    }
  }
}
