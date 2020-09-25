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

package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.CalculateForChild;
import org.anchoranalysis.feature.calculate.cache.CalculationResolver;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilation;

/** Calculates a dilated-object from a pair */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculateDilatedFromPair extends FeatureCalculation<ObjectMask, FeatureInputPairObjects> {

    // Not included in hash-coding as its assumed to be singular
    @EqualsAndHashCode.Exclude private CalculateForChild<FeatureInputPairObjects> resolverForChild;

    private ResolvedCalculation<FeatureInputSingleObject, FeatureInputPairObjects> calculation;
    private Extract extract;
    private ChildCacheName childCacheName;
    private int iterations;
    private boolean do3D;

    public static FeatureCalculation<ObjectMask, FeatureInputPairObjects> of(
            CalculationResolver<FeatureInputPairObjects> resolver,
            CalculateForChild<FeatureInputPairObjects> calculateForChild,
            Extract extract,
            ChildCacheName childCacheName,
            int iterations,
            boolean do3D) {
        return new CalculateDilatedFromPair(
                calculateForChild,
                resolver.search(new CalculateInputFromPair(extract)),
                extract,
                childCacheName,
                iterations,
                do3D);
    }

    @Override
    protected ObjectMask execute(FeatureInputPairObjects input) throws FeatureCalculationException {
        return resolverForChild.calculate(
                childCacheName,
                calculation.getOrCalculate(input),
                resolver -> CalculateDilation.of(resolver, iterations, do3D));
    }
}
