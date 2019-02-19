package be.vliz.emodnet.querytool.core.model;

import be.vliz.emodnet.querytool.core.model.feature.Geometry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlTransient
abstract class StatisticProperty implements Serializable {
  @XmlElements({
    @XmlElement(name="numeric", type=NumbericStatisticProperty.class),
    @XmlElement(name="distinctCount", type=DistinctCountStatisticProperty.class)
  })

  private static final long serialVersionUID = -7653929427418867968L;
  protected final String name;
  protected final Statistic statistic;


  protected StatisticProperty(String name, Statistic statistic) {
    this.name = name;
    this.statistic = statistic;
  }

  public String getName() {
    return name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Double min = Double.MAX_VALUE;
    private Double max = Double.MIN_VALUE;
    private Double total = 0.0d;
    private Map<String,Double> distinctValueCounter = new HashMap<>();
    private String name;

    private Builder() {
    }

    Builder of(StatisticProperty sp) {
      this.name(sp.name);
      if (sp instanceof NumbericStatisticProperty) {
        NumbericStatisticProperty nsp = (NumbericStatisticProperty) sp;
        this.min = nsp.getMin();
        this.max = nsp.getMax();
        this.total = nsp.getTotal();
      } else if (sp instanceof DistinctCountStatisticProperty) {
        DistinctCountStatisticProperty dcsp = (DistinctCountStatisticProperty) sp;
        this.distinctValueCounter = new HashMap<>(dcsp.getDistinctCounts());
      }

      return this;
    }

    Builder name(String name) {
      this.name = name;
      return this;
    }

    Builder add(Object property, Geometry geo) {
      if (property == null) {
        // noop
      } else if (property instanceof Number) {
        addNumber((Number) property);
      } else if (property instanceof StatisticProperty) {
        collectProperty((StatisticProperty) property);
      } else if (property instanceof StatisticProperty.Builder) {
        collectProperty(((StatisticProperty.Builder)property).build(null));
      } else if (property instanceof String) {
        try {
          addNumber(Double.parseDouble((String) property));
        } catch (NumberFormatException e) {
          double incr = geo == null || "Point".equals(geo.getType())  ? 1.0d : geo.surfaceArea();
          this.distinctValueCounter.put(
            property.toString(),
            this.distinctValueCounter.getOrDefault(property.toString(), 0.0d) + incr
          );
        }
      } else {
        throw new RuntimeException("No handler for type " + property.getClass().toString());
      }
      return this;
    }

    private boolean addNumber(Number n) {
      min = Math.min(n.doubleValue(), min);
      max = Math.max(n.doubleValue(), max);
      total += n.doubleValue();
      return true;
    }

    private boolean collectProperty(StatisticProperty sp) {
      if (sp instanceof NumbericStatisticProperty) {
        NumbericStatisticProperty nsp = (NumbericStatisticProperty) sp;
        min = Math.min(nsp.getMin(), min);
        max = Math.max(nsp.getMax(), max);
        total += nsp.getTotal();
      } else if (sp instanceof DistinctCountStatisticProperty) {
        DistinctCountStatisticProperty dcsp = (DistinctCountStatisticProperty) sp;
        dcsp.getDistinctCounts().forEach((key, value) ->
          this.distinctValueCounter.merge(key, value, (a, b) -> a + b)
        );
      }
      return true;
    }

    public boolean isValid() {
      return min <= max || !this.distinctValueCounter.isEmpty();
    }

    public StatisticProperty build(Statistic statistic) {
      if (!isValid()) {
        throw new java.lang.IllegalStateException(String.format("No values have been added to property {}", name));
      }
      if (min <= max) {
        return new NumbericStatisticProperty(name, statistic, min, max, total);
      } else {
        return new DistinctCountStatisticProperty(name, statistic, distinctValueCounter);
      }
    }
  }
}
