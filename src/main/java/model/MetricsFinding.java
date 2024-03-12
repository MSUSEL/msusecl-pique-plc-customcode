package model;

import lombok.Getter;
import lombok.Setter;
import pique.model.Finding;

public class MetricsFinding extends Finding {
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String metricName;
    @Getter @Setter
    private Double metricValue;

    public MetricsFinding(String filePath, String name, String metricName, Double metricValue, int severity) {
        super(filePath, 0, 0, severity);
        this.name = name;
        this.metricName = metricName;
        this.metricValue = metricValue;
    }
}
