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

package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 *
 * <p>Specifically, <code>relation(featureValue,threshold)</code> must be true.
 *
 * @author Owen Feehan
 */
public class ThresholdedFeature extends ObjectFilterRelation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;

    @BeanField @Getter @Setter private double threshold;

    @BeanField @Getter @Setter private boolean debug = true;
    // END BEAN PROPERTIES

    private FeatureCalculatorSingle<FeatureInputSingleObject> featureSession = null;

    @Override
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dimensions, objectsToFilter);

        // This could be called many times, so we create a new feature session only on the first
        // occasion
        if (featureSession == null) {
            featureSession = featureEvaluator.createFeatureSession();
        }

        if (debug) {
            getLogger().messageLogger().log("START Feature Threshold");
        }
    }

    @Override
    protected boolean match(ObjectMask object, Optional<Dimensions> dim, RelationToValue relation)
            throws OperationFailedException {

        double value;
        try {
            value = featureSession.calculate(new FeatureInputSingleObject(object));
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }

        boolean matched = relation.isRelationToValueTrue(value, threshold);

        if (debug) {
            if (matched) {
                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "%s\tVal=%f\tthreshold=%f\t  (Accepted)",
                                object.centerOfGravity(), value, threshold);
            } else {
                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "%s\tVal=%f\tthreshold=%f",
                                object.centerOfGravity(), value, threshold);
            }
        }

        return matched;
    }

    @Override
    protected void end() throws OperationFailedException {
        if (debug) {
            getLogger().messageLogger().log("END Feature Threshold");
        }
    }
}
