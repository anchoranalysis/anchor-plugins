/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriorityFromImprovement;

/**
 * Merges neighboring objects if it results in an increase in the average feature-value calculated
 * on each single object i.e.
 *
 * <pre>if feature(merged) >= avg( feature(src), feature(dest)</pre>
 *
 * <p>These merges occur in order of the maximum increase offered, and the algorithm recursively
 * merge until all possible merges are complete.
 *
 * <p>The feature-value is calculated for each object that is a vertex of the graph (including all
 * prospective merges of neighbors).
 *
 * @author Owen Feehan
 */
public class MergeToIncreaseObjectFeature extends MergeWithFeature {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
    // END BEAN PROPERTIES

    @Override
    protected PayloadCalculator createPayloadCalculator() throws OperationFailedException {

        FeatureCalculatorSingle<FeatureInputSingleObject> calculator =
                featureEvaluator.createAndStartSession();

        return object -> calculator.calc(new FeatureInputSingleObject(object));
    }

    @Override
    protected AssignPriority createPrioritizer() throws OperationFailedException {
        return new AssignPriorityFromImprovement(createPayloadCalculator());
    }

    @Override
    protected boolean isPlayloadUsed() {
        return true;
    }
}
