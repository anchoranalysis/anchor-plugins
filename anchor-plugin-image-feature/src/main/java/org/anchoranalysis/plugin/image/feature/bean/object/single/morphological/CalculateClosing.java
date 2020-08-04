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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculationMap;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateDilationMap;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class CalculateClosing extends FeatureCalculation<ObjectMask, FeatureInputSingleObject> {

    private final int iterations;
    private final ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> mapDilation;
    private final boolean do3D;

    public static ResolvedCalculation<ObjectMask, FeatureInputSingleObject> of(
            CalculationResolver<FeatureInputSingleObject> cache, int iterations, boolean do3D) {
        ResolvedCalculationMap<ObjectMask, FeatureInputSingleObject, Integer> map =
                cache.search(new CalculateDilationMap(do3D));

        return cache.search(new CalculateClosing(iterations, map, do3D));
    }

    @Override
    protected ObjectMask execute(FeatureInputSingleObject params)
            throws FeatureCalculationException {

        try {
            ObjectMask dilated = mapDilation.getOrCalculate(params, iterations);

            return MorphologicalErosion.createErodedObject(
                    dilated,
                    params.dimensionsOptional().map(ImageDimensions::extent),
                    do3D,
                    iterations,
                    false,
                    Optional.empty());

        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
