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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MinFeatureAsIndividual extends FeaturePairMemoOne {

    private static final ChildCacheName CACHE_NAME_FIRST =
            new ChildCacheName(MinFeatureAsIndividual.class, "first");
    private static final ChildCacheName CACHE_NAME_SECOND =
            new ChildCacheName(MinFeatureAsIndividual.class, "second");

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        return Math.min(calcForInd(input, true), calcForInd(input, false));
    }

    private double calcForInd(SessionInput<FeatureInputPairMemo> input, boolean first)
            throws FeatureCalcException {

        return input.forChild()
                .calc(
                        getItem(),
                        new CalculateDeriveSingleInputFromPair(first),
                        first ? CACHE_NAME_FIRST : CACHE_NAME_SECOND);
    }
}
