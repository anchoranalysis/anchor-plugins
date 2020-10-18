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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.ResolvedCalculation;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.object.calculation.CalculateInputFromPair.Extract;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.bean.morphological.MorphologicalIterations;
import org.anchoranalysis.spatial.extent.Extent;

/**
 * Procedure to calculate an area of intersection between two objects (termed first and second)
 *
 * <p>Specifically, this is achieved by:
 *
 * <ul>
 *   <li>dilating {@code first} by {@code iterationsFirst}
 *   <li>dilating {@code second} by {@code iterationsSecond}
 *   <li>finding the intersection of 1. and 2.
 *   <li>then eroding by {@code iterationsErosion}
 * </ul>
 *
 * <p>This is NOT commutative: {@code f(a,b)!=f(b,a)}
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculatePairIntersection
        extends FeatureCalculation<Optional<ObjectMask>, FeatureInputPairObjects> {

    private final boolean do3D;
    private final int iterationsErosion;
    private final ResolvedCalculation<ObjectMask, FeatureInputPairObjects> first;
    private final ResolvedCalculation<ObjectMask, FeatureInputPairObjects> second;

    public static ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects> of(
            SessionInput<FeatureInputPairObjects> cache,
            ChildCacheName cacheLeft,
            ChildCacheName cacheRight,
            MorphologicalIterations iterations,
            int iterationsSecond) {
        // We use two additional caches, for the calculations involving the single objects, as these
        // can be expensive, and we want
        //  them also cached
        return cache.resolver()
                .search(
                        new CalculatePairIntersection(
                                iterations.isDo3D(),
                                iterations.getIterationsErosion(),
                                cache.resolver()
                                        .search(
                                                CalculateDilatedFromPair.of(
                                                        cache.resolver(),
                                                        cache.forChild(),
                                                        Extract.FIRST,
                                                        cacheLeft,
                                                        iterations.getIterationsDilation(),
                                                        iterations.isDo3D())),
                                cache.resolver()
                                        .search(
                                                CalculateDilatedFromPair.of(
                                                        cache.resolver(),
                                                        cache.forChild(),
                                                        Extract.SECOND,
                                                        cacheRight,
                                                        iterationsSecond,
                                                        iterations.isDo3D()))));
    }

    @Override
    protected Optional<ObjectMask> execute(FeatureInputPairObjects input)
            throws FeatureCalculationException {

        Extent extent = input.dimensionsRequired().extent();

        ObjectMask object1Dilated = first.getOrCalculate(input);
        ObjectMask object2Dilated = second.getOrCalculate(input);

        Optional<ObjectMask> omIntersection = object1Dilated.intersect(object2Dilated, extent);

        if (!omIntersection.isPresent()) {
            return Optional.empty();
        }

        assert (omIntersection.get().voxelsOn().anyExists());

        try {
            if (iterationsErosion > 0) {
                return erode(input, omIntersection.get(), extent);
            } else {
                return omIntersection;
            }

        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private Optional<ObjectMask> erode(
            FeatureInputPairObjects input, ObjectMask intersection, Extent extent)
            throws CreateException {

        // We erode it, and use this as a mask on the input object
        ObjectMask eroded =
                MorphologicalErosion.createErodedObject(
                        input.getMerged(),
                        Optional.of(extent),
                        do3D,
                        iterationsErosion,
                        true,
                        Optional.empty());

        return intersection.intersect(eroded, extent);
    }
}
