/*-
 * #%L
 * anchor-plugin-image-feature
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
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.Divide;

/**
 * Calculates the aggregate of the ratio of <code>first</code>:<code>second</code> and <code>second
 * </code>:<code>first</code> for a {@link FeatureInputPairObjects}
 *
 * <p><code>first</code> and <code>second</code> refer respectively to a feature calculated on the
 * first and second objects of a {@link FeatureInputPairObjects}.
 *
 * @author Owen Feehan
 */
public class ReduceRatioFirstAndSecond extends FeatureListProviderAggregatePair {

    @Override
    protected Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged) {
        return createReducedFeature(
                ratioTwoFeatures(first, second), ratioTwoFeatures(second, first) // NOSONAR
                );
    }

    private static <T extends FeatureInput> Feature<T> ratioTwoFeatures(
            Feature<T> first, Feature<T> second) {
        Divide<T> out = new Divide<>();
        out.setAvoidDivideByZero(true);
        out.getList().add(first);
        out.getList().add(second);
        return out;
    }
}
