package evaluation;

import model.MetricFinding;
import pique.evaluation.Evaluator;
import pique.model.ModelNode;
import pique.utility.BigDecimalWithContext;

import java.math.BigDecimal;

public class MetricFindingEvaluator extends Evaluator {

    @Override
    public BigDecimal evaluate(ModelNode inNode) {
        MetricFinding node = (MetricFinding) inNode;
        return new BigDecimalWithContext(node.getMetricValue());
    }
}
