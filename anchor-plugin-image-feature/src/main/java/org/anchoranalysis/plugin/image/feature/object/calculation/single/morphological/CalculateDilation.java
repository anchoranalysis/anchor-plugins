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

package org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPartMap;
import org.anchoranalysis.feature.calculate.part.CalculationPartResolver;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.CalculateObjectMask;

/**
 * Calculates the dilation of an {@link ObjectMask}.
 *
 * <p>This class extends {@link CalculateObjectMask} to perform dilation operations on object masks.
 */
@EqualsAndHashCode(callSuper = true)
public class CalculateDilation extends CalculateObjectMask {

    /**
     * Creates a new {@link ResolvedPart} for calculating dilation.
     *
     * @param cache the {@link CalculationPartResolver} to use for caching
     * @param iterations the number of dilation iterations to perform
     * @param do3D whether to perform 3D dilation (true) or 2D dilation (false)
     * @return a {@link ResolvedPart} for the dilation calculation
     */
    public static ResolvedPart<ObjectMask, FeatureInputSingleObject> of(
            CalculationPartResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedPartMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateDilationMap(do3D));

        return cache.search(new CalculateDilation(iterations, map));
    }

    /**
     * Private constructor for {@link CalculateDilation}.
     *
     * @param iterations the number of dilation iterations to perform
     * @param map the {@link ResolvedPartMap} for caching dilation results
     */
    private CalculateDilation(
            int iterations, ResolvedPartMap<ObjectMask, FeatureInputSingleObject, Integer> map) {
        super(iterations, map);
    }
}
