/**
 * MIT License
 *
 * Copyright (c) 2024 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package model;

import lombok.Getter;
import pique.evaluation.IEvaluator;
import pique.evaluation.INormalizer;
import pique.evaluation.IUtilityFunction;
import pique.model.Diagnostic;
import pique.model.ModelNode;
import tool.CODESYSWrapper;

import java.math.BigDecimal;
import java.util.HashMap;
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
    @Override
    public ModelNode clone() {

        Map<String, ModelNode> clonedChildren = new HashMap<>();
        getChildren().forEach((k, v) -> clonedChildren.put(k, v.clone()));

        return new CODESYSRuleDiagnostic(getValue(), getName(), getDescription(), this.getEval_strategyObj(), this.getNormalizerObj(),
                this.getUtility_function(), getWeights(), getThresholds(), clonedChildren, this.importance);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CODESYSRuleDiagnostic)) { return false; }
        CODESYSRuleDiagnostic otherDiagnostic = (CODESYSRuleDiagnostic) other;

        return getName().equals(otherDiagnostic.getName())
                && getToolName().equals(otherDiagnostic.getToolName());
    }
}
