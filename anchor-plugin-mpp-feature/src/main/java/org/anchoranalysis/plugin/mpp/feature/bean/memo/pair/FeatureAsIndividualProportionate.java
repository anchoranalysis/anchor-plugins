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

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.CalculateDeriveSingleMemoFromPair;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import lombok.Getter;
import lombok.Setter;

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
    private class CalcHelper {

        private FeatureCalculation<FeatureInputSingleMemo, FeatureInputPairMemo> ccExtract;
        private ChildCacheName childCacheName;

        public CalcHelper(
                FeatureCalculation<FeatureInputSingleMemo, FeatureInputPairMemo> ccExtract,
                ChildCacheName childCacheName) {
            super();
            this.ccExtract = ccExtract;
            this.childCacheName = childCacheName;
        }

        public double valueFor(SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalcException {
            return calcFeatureFor(getItem(), params);
        }

        public double weightFor(SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalcException {
            return calcFeatureFor(itemProportionate, params);
        }

        private double calcFeatureFor(
                Feature<FeatureInputSingleMemo> feature, SessionInput<FeatureInputPairMemo> params)
                throws FeatureCalcException {
            return params.forChild().calc(feature, ccExtract, childCacheName);
        }
    }

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        CalcHelper first =
                new CalcHelper(new CalculateDeriveSingleMemoFromPair(true), CACHE_NAME_FIRST);
        CalcHelper second =
                new CalcHelper(new CalculateDeriveSingleMemoFromPair(false), CACHE_NAME_SECOND);

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
