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
package org.anchoranalysis.plugin.image.feature.bean.object.single;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.kernel.KernelApplicationParameters;
import org.anchoranalysis.image.voxel.kernel.OutsideKernelPolicy;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/** Base class for features that apply a kernel to the outline of an object. */
public abstract class OutlineKernelBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** If true, considers voxels outside the object at the threshold as part of the object. */
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    /** If true, ignores voxels at the threshold when applying the kernel. */
    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    /** If true, applies the kernel in 3D; otherwise, applies it in 2D. */
    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES

    @Override
    protected double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        FeatureInputSingleObject inputSessionless = input.get();
        return calculateWithParameters(
                inputSessionless.getObject(),
                createParameters(),
                inputSessionless::getEnergyStackRequired);
    }

    /**
     * Calculates the feature value using the specified parameters.
     *
     * @param object the {@link ObjectMask} to calculate the feature on
     * @param parameters the {@link KernelApplicationParameters} for applying the kernel
     * @param energyStack a supplier for the {@link EnergyStack}
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateWithParameters(
            ObjectMask object,
            KernelApplicationParameters parameters,
            CheckedSupplier<EnergyStack, FeatureCalculationException> energyStack)
            throws FeatureCalculationException;

    /**
     * Creates the {@link KernelApplicationParameters} based on the bean properties.
     *
     * @return the created {@link KernelApplicationParameters}
     */
    private KernelApplicationParameters createParameters() {
        return new KernelApplicationParameters(
                OutsideKernelPolicy.of(ignoreAtThreshold, outsideAtThreshold), do3D);
    }
}
