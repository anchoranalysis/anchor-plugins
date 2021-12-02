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

package org.anchoranalysis.plugin.image.feature.bean.object.single.morphological;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPartMap;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.calculate.part.CalculationPartResolver;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilationMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculateClosing extends CalculationPart<ObjectMask, FeatureInputSingleObject> {

    private final int iterations;
    private final ResolvedPartMap<ObjectMask, FeatureInputSingleObject, Integer> mapDilation;
    private final boolean do3D;

    public static ResolvedPart<ObjectMask, FeatureInputSingleObject> of(
            CalculationPartResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedPartMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateDilationMap(do3D));

        return cache.search(new CalculateClosing(iterations, map, do3D));
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        try {
            ObjectMask dilated = mapDilation.getOrCalculate(input, iterations);

            return MorphologicalErosion.erode(dilated, iterations, do3D);

        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
