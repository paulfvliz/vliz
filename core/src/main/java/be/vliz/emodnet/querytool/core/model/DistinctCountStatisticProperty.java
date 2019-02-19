package be.vliz.emodnet.querytool.core.model;

import com.google.common.collect.ImmutableMap;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

@XmlRootElement(name="distinctCount")
public class DistinctCountStatisticProperty extends StatisticProperty implements Serializable {
  private static final long serialVersionUID = 8967059607121508666L;
  private Map<String, Double> distinctCounts;

  protected DistinctCountStatisticProperty(String name, Statistic statistic, Map<String, Double> distinctCounts) {
    super(name, statistic);
    this.distinctCounts = ImmutableMap.copyOf(distinctCounts);
  }

  public Map<String, Double> getDistinctCounts() {
    return distinctCounts;
  }
}
