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

package org.anchoranalysis.plugin.image.object.merge.condition;

import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * An {@link AfterCondition} that checks if merging two {@link ObjectMask}s increases a specific
 * feature value.
 */
@RequiredArgsConstructor
public class IncreaseFeatureCondition implements AfterCondition {

    /** The {@link FeatureEvaluator} used to calculate the feature value. */
    private final FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;

    /** The feature calculation session. */
    private FeatureCalculatorSingle<FeatureInputSingleObject> session;

    @Override
    public void initialize(Logger logger) throws InitializeException {

        if (featureEvaluator != null) {
            try {
                session = featureEvaluator.createFeatureSession();
            } catch (OperationFailedException e) {
                throw new InitializeException(e);
            }
        } else {
            session = null;
        }
    }

    @Override
    public boolean accept(ObjectMask source, ObjectMask destination, ObjectMask merged)
            throws OperationFailedException {

        if (session != null) {
            return doesIncreaseFeatureValueForBoth(source, destination, merged);
        } else {
            return true;
        }
    }

    /**
     * Checks if merging two objects increases the feature value for both.
     *
     * @param source the source {@link ObjectMask}
     * @param destination the destination {@link ObjectMask}
     * @param merged the merged {@link ObjectMask}
     * @return true if the feature value increases for both objects after merging, false otherwise
     * @throws OperationFailedException if the feature calculation fails
     */
    private boolean doesIncreaseFeatureValueForBoth(
            ObjectMask source, ObjectMask destination, ObjectMask merged)
            throws OperationFailedException {

        // Feature source
        try {
            double featureSrc = calculate(source);
            double featureDest = calculate(destination);
            double featureMerge = calculate(merged);

            // We skip if we don't increase the feature value for both objects
            return (featureMerge > featureSrc && featureMerge > featureDest);

        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates the feature value for a given {@link ObjectMask}.
     *
     * @param object the {@link ObjectMask} to calculate the feature value for
     * @return the calculated feature value
     * @throws FeatureCalculationException if the feature calculation fails
     */
    private double calculate(ObjectMask object) throws FeatureCalculationException {
        return session.calculate(new FeatureInputSingleObject(object));
    }
}
