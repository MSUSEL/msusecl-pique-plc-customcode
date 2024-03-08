package model;

import lombok.Getter;
import lombok.Setter;
import pique.model.Finding;

public class RuleFinding extends Finding {
    @Getter @Setter
    private String ruleId;
    @Getter @Setter
    private String description;
    public RuleFinding(String filePath, String ruleId, String description, int severity) {
        super(filePath, 0, 0, severity);
        this.ruleId = ruleId;
        this.description = description;
    }
}
