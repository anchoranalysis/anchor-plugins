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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.calculate.part.CalculationPartResolver;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.morphological.CalculateErosion;

/**
 * A feature that erodes an object mask and then calculates another feature on the eroded object.
 */
public class Erode extends DerivedObject {

    /** The number of iterations to perform the erosion operation. */
    @BeanField @Getter @Setter private int iterations;

    /** If true, performs 3D erosion; if false, performs 2D erosion on each z-slice separately. */
    @BeanField @Getter @Setter private boolean do3D = true;

    /**
     * Creates a {@link CalculationPart} for eroding the object mask.
     *
     * @param session the {@link CalculationPartResolver} for resolving calculation parts
     * @return a {@link CalculationPart} that erodes an {@link ObjectMask}
     */
    @Override
    protected CalculationPart<ObjectMask, FeatureInputSingleObject>
            createCachedCalculationForDerived(
                    CalculationPartResolver<FeatureInputSingleObject> session) {
        return CalculateErosion.of(session, iterations, do3D);
    }

    /**
     * Provides a unique name for caching the eroded object.
     *
     * @return a {@link ChildCacheName} for caching the eroded object
     */
    @Override
    public ChildCacheName cacheName() {
        return new ChildCacheName(Erode.class, iterations + "_" + do3D);
    }
}
