/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.priority;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/**
 * Allows merges if there is an increase in the payload i.e.
 *
 * <pre>if payload(merged) >= avg( payload(src), payload(dest )</pre>
 *
 * <p>Prioritises merges in order of greatest improvement.
 *
 * @author Owen Feehan
 */
public class AssignPriorityFromImprovement extends AssignPriority {

    private PayloadCalculator payloadCalculator;

    public AssignPriorityFromImprovement(PayloadCalculator payloadCalculator) {
        super();
        this.payloadCalculator = payloadCalculator;
    }

    @Override
    public PrioritisedVertex assignPriorityToEdge(
            ObjectVertex src, ObjectVertex dest, ObjectMask merged, ErrorReporter errorReporter)
            throws OperationFailedException {
        double payloadMerge = calcPayload(payloadCalculator, merged);
        double payloadExisting = weightedAverageFeatureVal(src, dest);
        double improvement = payloadMerge - payloadExisting;

        if (payloadMerge <= payloadExisting) {
            // We add an edge anyway, as a placeholder as future merges might make it viable
            return new PrioritisedVertex(merged, payloadMerge, improvement, false);
        }

        return new PrioritisedVertex(merged, payloadMerge, improvement, true);
    }

    private double calcPayload(PayloadCalculator payloadCalculator, ObjectMask object)
            throws OperationFailedException {
        try {
            return payloadCalculator.calc(object);
        } catch (FeatureCalcException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * A weighted-average of the feature value (weighted according to the relative number of pixeks
     */
    private static double weightedAverageFeatureVal(ObjectVertex first, ObjectVertex second) {
        long size1 = first.numberVoxels();
        long size2 = second.numberVoxels();

        double weightedSum = (first.getPayload() * size1) + (second.getPayload() * size2);
        return weightedSum / (size1 + size2);
    }
}
