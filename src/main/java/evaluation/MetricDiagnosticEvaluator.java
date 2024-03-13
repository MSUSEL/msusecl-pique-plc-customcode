package evaluation;

import pique.evaluation.Evaluator;
import pique.model.ModelNode;
import pique.utility.BigDecimalWithContext;

import java.math.BigDecimal;

public class MetricDiagnosticEvaluator extends Evaluator {


    @Override
    public BigDecimal evaluate(ModelNode inNode) {

        BigDecimal rawSum = new BigDecimalWithContext("0.0");

        for (ModelNode x : inNode.getChildren().values()) {
            rawSum = rawSum.add(x.getValue());
        }
        if (inNode.getNumChildren() == 0){
            return rawSum;
        }
        return rawSum.divide(new BigDecimalWithContext(inNode.getNumChildren()), BigDecimalWithContext.getMC());
    }
}
