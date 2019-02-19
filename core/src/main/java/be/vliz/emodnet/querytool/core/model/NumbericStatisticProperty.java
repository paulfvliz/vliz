package be.vliz.emodnet.querytool.core.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="numeric")
public class NumbericStatisticProperty extends StatisticProperty implements Serializable {
  private static final long serialVersionUID = -3993467443366362204L;
  private final Double min;
  private final Double max;
  private final Double total;

  protected NumbericStatisticProperty(String name, Statistic statistic, Double min, Double max, Double total) {
    super(name, statistic);
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
}


