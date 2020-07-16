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
/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.conditional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * The result of featureCondition is compared to a threshold, and then either the underlying feature
 * is calculated (positive case), or featureElse is (negative case)
 *
 * <p>The positive case is when relation(featureCondition,value) is TRUE..
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class IfCondition<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIRES
    @BeanField private Feature<T> featureCondition;

    @BeanField private Feature<T> featureElse;

    @BeanField private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        double featureConditionResult = input.calc(featureCondition);
        RelationToValue relation = threshold.relation();

        if (relation.isRelationToValueTrue(featureConditionResult, threshold.threshold())) {
            return input.calc(super.getItem());
        } else {
            return input.calc(featureElse);
        }
    }

    public Feature<T> getFeatureCondition() {
        return featureCondition;
    }

    public void setFeatureCondition(Feature<T> featureCondition) {
        this.featureCondition = featureCondition;
    }

    public Feature<T> getFeatureElse() {
        return featureElse;
    }

    public void setFeatureElse(Feature<T> featureElse) {
        this.featureElse = featureElse;
    }

    public RelationToThreshold getThreshold() {
        return threshold;
    }

    public void setThreshold(RelationToThreshold threshold) {
        this.threshold = threshold;
    }
}
