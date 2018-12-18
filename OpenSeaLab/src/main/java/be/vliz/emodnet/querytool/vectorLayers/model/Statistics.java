package be.vliz.emodnet.querytool.vectorLayers.model;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistics {
    private final Map<String, Double> statistics;

    public Statistics(Map<String, Double> statistics) {
        this.statistics = statistics;
    }

    public Map<String, Double> getStatistics() {
        return statistics.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
