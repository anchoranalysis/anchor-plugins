/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
 * objects i.e. {@code feature(merged) > feature(object1)} and {@code feature(merged) > feature(object2)}.
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
                            resolutionOptional(),
                            getLogger());

            return mergeMultiplex(objectsSource, merger::tryMerge);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
