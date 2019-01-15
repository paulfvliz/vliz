package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Feature;
import be.vliz.emodnet.querytool.core.model.feature.Geometry;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class Statistic implements Serializable {
  private static final long serialVersionUID = 8922647429954334337L;
  private final String name;
  private final Statistics parent;
  private final double total;
  private final ImmutableMap<String, StatisticProperty> properties;

  private Statistic(String name, Statistics parent, double total, Map<String, StatisticProperty.Builder> properties) {
    this.name = name;
    this.parent = parent;
    this.total = total;
    this.properties = properties.entrySet().stream()
      .filter(e -> e.getValue().isValid())
      .collect(ImmutableMap.toImmutableMap(
        Map.Entry::getKey,
        e -> e.getValue().build(this)
      ));
  }

  public double getTotal() {
    return total;
  }

  public double getRatio() {
    return this.total / parent.getTotal();
  }

  public Map<String, StatisticProperty> getProperties() {
    return properties;
  }

  public String getName() {
    return name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private Double total = 0.0d;
    private Map<String, StatisticProperty.Builder> properties = new HashMap<>();


    private Builder() {
    }

    private double increment(double i) {
      return this.total += i;
    }

    Builder of(Statistic s) {
      this.name(s.name);
      this.total = s.total;
      this.properties = new HashMap<>(s.properties.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> StatisticProperty.builder().of(e.getValue())
        )));
      return this;
    }

    Builder name(String name) {
      this.name = name;
      return this;
    }

    Builder add(Feature f) {
      Geometry geo = f.getGeometry();
      if (geo == null)
        return this;

      if (geo.getType().equals("Point")) {
        this.increment(1.0d);
      } else {
        this.increment(geo.surfaceArea());
      }

      f.getProperties().entrySet().forEach(entry -> {
        StatisticProperty.Builder spb = properties.getOrDefault(entry.getKey(), StatisticProperty.builder().name(entry.getKey()));
        spb.add(entry.getValue());
        properties.putIfAbsent(entry.getKey(), spb);
      });
      return this;
    }

    Builder add(Statistic s) {
      for (StatisticProperty sp : s.properties.values()) {
        StatisticProperty.Builder spb = StatisticProperty.builder().of(sp);
        this.properties.merge(
          sp.getName(),
          spb,
          (newValue, value) -> value.add(newValue));
      }
      increment(s.total);
      return this;
    }

    public boolean isValid() {
      return name != null;
    }

    Statistic build(Statistics parent) {
      if (! isValid()) {
        throw new IllegalStateException();
      }
      return new Statistic(name, parent, total, properties);
    }
  }
}
