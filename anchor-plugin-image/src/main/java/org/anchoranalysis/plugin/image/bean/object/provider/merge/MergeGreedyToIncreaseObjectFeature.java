/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.object.merge.condition.IncreaseFeatureCondition;

/**
 * Naive greedy merge strategy when any two neighboring objects are merged if it increases a
 * feature.
 *
 * <p>No guarantee exists over the priority over which merges occur, so any inferior merge could
 * occur before a superior one, as long as both merges fulfill the conditions.
 *
 * <p>A merge occurs if the feature is increased in the merge object compared to both pre-merged
 * objects i.e.
 *
 * <pre>feature(merged) > feature(object1)</pre>
 *
 * and
 *
 * <pre>feature(merged) > feature(object2)</pre>
 *
 * @author Owen Feehan
 */
public class MergeGreedyToIncreaseObjectFeature extends MergeWithOptionalDistanceConstraint {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean replaceWithMidpoint = false;

    @BeanField @OptionalBean @Getter @Setter
    private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws CreateException {
        try {
            NaiveGreedyMerge merger =
                    new NaiveGreedyMerge(
                            replaceWithMidpoint,
                            maybeDistanceCondition(),
                            new IncreaseFeatureCondition(featureEvaluator),
                            calcResOptional(),
                            getLogger());

            return mergeMultiplex(objectsSource, merger::tryMerge);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
