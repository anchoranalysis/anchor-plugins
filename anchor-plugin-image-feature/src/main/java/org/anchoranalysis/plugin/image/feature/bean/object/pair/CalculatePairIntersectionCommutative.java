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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.morphological.MorphologicalIterations;

/**
 * Finds the intersection between the dilated versions of two objects (and then performs some
 * erosion)
 * <li>Each object-mask is dilated by (determined by iterationsDilation)
 * <li>The intersection is found
 * <li>Then erosion occurs (determined by iterationsErosion)
 *
 *     <p>This is commutative: {@code f(a,b)==f(b,a)}
 *
 *     <p>
 *
 * @author Owen Feehan
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculatePairIntersectionCommutative
        extends CalculationPart<Optional<ObjectMask>, FeatureInputPairObjects> {

    private final ResolvedPart<Optional<ObjectMask>, FeatureInputPairObjects> ccFirstToSecond;
    private final ResolvedPart<Optional<ObjectMask>, FeatureInputPairObjects> ccSecondToFirst;

    public static CalculationPart<Optional<ObjectMask>, FeatureInputPairObjects> of(
            FeatureCalculationInput<FeatureInputPairObjects> cache,
            ChildCacheName childDilation1,
            ChildCacheName childDilation2,
            MorphologicalIterations iterations) {

        // We use two additional caches, for the calculations involving the single objects, as these
        // can be expensive, and we want
        //  them also cached
        ResolvedPart<Optional<ObjectMask>, FeatureInputPairObjects> ccFirstToSecond =
                CalculatePairIntersection.of(cache, childDilation1, childDilation2, iterations, 0);
        ResolvedPart<Optional<ObjectMask>, FeatureInputPairObjects> ccSecondToFirst =
                CalculatePairIntersection.of(
                        cache,
                        childDilation1,
                        childDilation2,
                        iterations.copyChangeIterationsDilation(0),
                        iterations.getIterationsDilation());
        return new CalculatePairIntersectionCommutative(ccFirstToSecond, ccSecondToFirst);
    }

    @Override
    protected Optional<ObjectMask> execute(FeatureInputPairObjects input)
            throws FeatureCalculationException {

        Optional<ObjectMask> omIntersection1 = ccFirstToSecond.getOrCalculate(input);
        Optional<ObjectMask> omIntersection2 = ccSecondToFirst.getOrCalculate(input);

        if (!omIntersection1.isPresent()) {
            return omIntersection2;
        }

        if (!omIntersection2.isPresent()) {
            return omIntersection1;
        }

        return Optional.of(ObjectMaskMerger.merge(omIntersection1.get(), omIntersection2.get()));
    }
}
