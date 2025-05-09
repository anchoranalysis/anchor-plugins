/*-
 * #%L
 * anchor-plugin-operator-feature
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

package org.anchoranalysis.plugin.operator.feature.bean.conditional;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureUnaryGeneric;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.math.relation.DoubleBiPredicate;

/**
 * The result of featureCondition is compared to a threshold, and then either the underlying feature
 * is calculated (positive case), or featureElse is (negative case)
 *
 * <p>The positive case is when relation(featureCondition,value) is true..
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class IfCondition<T extends FeatureInput> extends FeatureUnaryGeneric<T> {

    // START BEAN PROPERTIRES
    @BeanField @Getter @Setter private Feature<T> featureCondition;

    @BeanField @Getter @Setter private Feature<T> featureElse;

    @BeanField @Getter @Setter private RelationToThreshold threshold;

    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {

        double featureConditionResult = input.calculate(featureCondition);
        DoubleBiPredicate relation = threshold.relation();

        if (relation.test(featureConditionResult, threshold.threshold())) {
            return input.calculate(super.getItem());
        } else {
            return input.calculate(featureElse);
        }
    }
}
