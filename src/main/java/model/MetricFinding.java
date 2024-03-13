package model;

import lombok.Getter;
import lombok.Setter;
import pique.model.Finding;

public class MetricFinding extends Finding {

    @Getter @Setter
    private Double metricValue;

    public MetricFinding(String filePath, Double metricValue, int severity) {
        super(filePath, 0, 0, severity);
        this.metricValue = metricValue;
    }
}
