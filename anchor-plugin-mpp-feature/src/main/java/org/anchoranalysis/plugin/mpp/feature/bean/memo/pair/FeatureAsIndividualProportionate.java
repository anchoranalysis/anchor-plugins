/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.mpp.feature.bean.energy.element.CalculateDeriveSingleMemoFromPair;
import org.anchoranalysis.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.mpp.feature.input.memo.FeatureInputSingleMemo;

/**
 * Calculates each feature individually, and combines them using the ratios between
 * itemProportionate as weights
 */
public class FeatureAsIndividualProportionate extends FeaturePairMemoOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputSingleMemo> itemProportionate;
    // eND BEAN PROPERTIES

    private static final ChildCacheName CACHE_NAME_FIRST =
            new ChildCacheName(FeatureAsIndividualProportionate.class, "first");
    private static final ChildCacheName CACHE_NAME_SECOND =
            new ChildCacheName(FeatureAsIndividualProportionate.class, "second");

    /** Calculates values/weights for one of the objects */
    @AllArgsConstructor
    private class CalculateHelper {

        private FeatureCalculation<FeatureInputSingleMemo, FeatureInputPairMemo> ccExtract;
        private ChildCacheName childCacheName;

        public double valueFor(SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalculationException {
            return calculateFeatureFor(getItem(), params);
        }

        public double weightFor(SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalculationException {
            return calculateFeatureFor(itemProportionate, params);
        }

        private double calculateFeatureFor(
                Feature<FeatureInputSingleMemo> feature, SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalculationException {
            return params.forChild().calculate(feature, ccExtract, childCacheName);
        }
    }

    @Override
    public double calculate(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {

        CalculateHelper first =
                new CalculateHelper(new CalculateDeriveSingleMemoFromPair(true), CACHE_NAME_FIRST);
        CalculateHelper second =
                new CalculateHelper(
                        new CalculateDeriveSingleMemoFromPair(false), CACHE_NAME_SECOND);

        return weightedSum(
                first.valueFor(input),
                second.valueFor(input),
                first.weightFor(input),
                second.weightFor(input));
    }

    private static double weightedSum(double val1, double val2, double weight1, double weight2) {
        // Normalise
        double weightSum = weight1 + weight2;
        weight1 /= weightSum;
        weight2 /= weightSum;

        return (weight1 * val1) + (weight2 * val2);
    }
}
