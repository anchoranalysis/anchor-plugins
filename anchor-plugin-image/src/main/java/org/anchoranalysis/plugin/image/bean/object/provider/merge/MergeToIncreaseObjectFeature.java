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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.object.merge.priority.AssignPriorityFromImprovement;

/**
 * Merges neighboring objects if it results in an increase in the average feature-value calculated
 * on each single object
 *
 * <p>A merge occurs if {@code feature(merged) >= avg( feature(object1), feature(object2)}
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
                featureEvaluator.createFeatureSession();

        return object -> calculator.calculate(new FeatureInputSingleObject(object));
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
