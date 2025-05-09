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

package org.anchoranalysis.plugin.image.feature.bean.object.single.border;

import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.KernelApplicationParameters;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.object.single.OutlineKernelBase;

/**
 * Calculates the number of voxels at the border of an object.
 *
 * <p>This feature applies an outline kernel to the object and counts the number of voxels that are
 * part of the object's border.
 */
public class NumberVoxelsAtBorder extends OutlineKernelBase {

    @Override
    protected double calculateWithParameters(
            ObjectMask object,
            KernelApplicationParameters parameters,
            CheckedSupplier<EnergyStack, FeatureCalculationException> energyStack)
            throws FeatureCalculationException {
        return numberBorderPixels(object, parameters);
    }

    /**
     * Calculates the number of border voxels for a given object.
     *
     * @param object the {@link ObjectMask} to analyze
     * @param parameters the {@link KernelApplicationParameters} for applying the kernel
     * @return the number of voxels at the border of the object
     */
    public static int numberBorderPixels(
            ObjectMask object, KernelApplicationParameters parameters) {
        OutlineKernel kernel = new OutlineKernel();
        return ApplyKernel.applyForCount(kernel, object.binaryVoxels(), parameters);
    }
}
