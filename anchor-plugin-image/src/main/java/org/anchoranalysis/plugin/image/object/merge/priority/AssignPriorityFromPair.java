/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.priority;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/**
 * Calculates pair-feature on each potential merge, and this value determines priority..
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class AssignPriorityFromPair extends AssignPriority {

    private final FeatureCalculatorSingle<FeatureInputPairObjects> featureCalculator;
    private final double threshold;
    private final RelationToValue relation;

    @Override
    public PrioritisedVertex assignPriorityToEdge(
            ObjectVertex source,
            ObjectVertex destination,
            ObjectMask merged,
            ErrorReporter errorReporter)
            throws OperationFailedException {

        double resultPair =
                featureCalculator.calcSuppressErrors(
                        createInput(source, destination, merged), errorReporter);

        return new PrioritisedVertex(
                merged, 0, resultPair, relation.isRelationToValueTrue(resultPair, threshold));
    }

    private FeatureInputPairObjects createInput(
            ObjectVertex sourceWithFeature,
            ObjectVertex destinationWithFeature,
            ObjectMask merged) {
        return new FeatureInputPairObjects(
                sourceWithFeature.getObject(),
                destinationWithFeature.getObject(),
                Optional.empty(),
                Optional.of(merged));
    }
}
