package be.vliz.emodnet.querytool.core.model;

import java.io.Serializable;

class StatisticProperty implements Serializable {
  private static final long serialVersionUID = -7653929427418867968L;
  private final String name;
  private final Statistic statistic;
  private final Double min;
  private final Double max;
  private final Double total;

  private StatisticProperty(String name, Statistic statistic, Double min, Double max, Double total) {
    this.name = name;
    this.statistic = statistic;
    this.min = min;
    this.max = max;
    this.total = total;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public Double getTotal() {
    return total;
  }

  public Double getAvg() {
    return this.getTotal() / this.statistic.getTotal();
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
    private String name;

    private Builder() {
    }

    Builder of(StatisticProperty sp) {
      this.name(sp.name);
      this.min = sp.min;
      this.max = sp.max;
      this.total = sp.total;
      return this;
    }

    Builder name(String name) {
      this.name = name;
      return this;
    }

    Builder add(Object property) {
      if (property instanceof Number) {
        addNumber((Number) property);
      } else if (property instanceof StatisticProperty) {
        collectProperty((StatisticProperty) property);
      } else if (property instanceof String) {
        try {
          addNumber(Double.parseDouble((String) property));
        } catch (NumberFormatException e) {
          // noop
        }
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
      min = Math.min(sp.min, min);
      max = Math.max(sp.max, max);
      total += sp.total;
      return true;
    }

    public boolean isValid() {
      return min <= max;
    }

    public StatisticProperty build(Statistic statistic) {
      if (!isValid()) {
        throw new java.lang.IllegalStateException(String.format("No values have been added to property {}", name));
      }
      return new StatisticProperty(name, statistic, min, max, total);
    }
  }
}
