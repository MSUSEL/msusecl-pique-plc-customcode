package model;

import lombok.Getter;
import pique.evaluation.IEvaluator;
import pique.evaluation.INormalizer;
import pique.evaluation.IUtilityFunction;
import pique.model.Diagnostic;
import pique.model.ModelNode;

import java.math.BigDecimal;
import java.util.Map;

public class CODESYSRuleDiagnostic extends Diagnostic {

    // importance is featured in CODESYS documentation but not as output from the tool. We manually copied the importances
    // from the documentation into the model definition, and we have to incorporate it into a Diagnostic object before we can
    // assign its value to findings
    @Getter
    private String importance;

    public CODESYSRuleDiagnostic(String id, String description, String toolName, String importance) {
        super(id, description, toolName);
        this.importance = importance;
    }

    public CODESYSRuleDiagnostic(String id, String description, String toolName, IEvaluator evaluator, String importance) {
        super(id, description, toolName, evaluator);
        this.importance = importance;
    }

    public CODESYSRuleDiagnostic(String id, String description, String toolName, IEvaluator evaluator, INormalizer normalizer, IUtilityFunction utilityFunction, Map<String, BigDecimal> weights, BigDecimal[] thresholds, String importance) {
        super(id, description, toolName, evaluator, normalizer, utilityFunction, weights, thresholds);
        this.importance = importance;
    }

    public CODESYSRuleDiagnostic(BigDecimal value, String name, String description, IEvaluator evaluator, INormalizer normalizer, IUtilityFunction utilityFunction, Map<String, BigDecimal> weights, BigDecimal[] thresholds, Map<String, ModelNode> children, String importance) {
        super(value, name, description, evaluator, normalizer, utilityFunction, weights, thresholds, children);
        this.importance = importance;
    }
}
