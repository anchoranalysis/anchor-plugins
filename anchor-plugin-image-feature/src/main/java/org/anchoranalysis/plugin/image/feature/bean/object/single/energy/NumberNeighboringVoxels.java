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

package org.anchoranalysis.plugin.image.feature.bean.object.single.energy;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelTypeException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernelNeighborMatchValue;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernelParameters;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.object.single.OutlineKernelBase;

/**
 * Calculates the number of voxels on the object that have a neighbor.
 *
 * <p>What possible neighbors are specified by a binary-mask from an energy-channel. This should
 * have with 255 high, and 0 low, and indicate all possible neighbor voxels. The region on the
 * binary-mask coinciding with the object is irrelevant and be set to any value.
 *
 * @author Owen Feehan
 */
public class NumberNeighboringVoxels extends OutlineKernelBase {

    // START BEAN PROPERTIES
    /** Index of which channel in the energy-stack to select */
    @BeanField @Getter @Setter private int energyIndex = 0;
    // END BEAN PROPERTIES

    @Override
    protected double calculateWithParameters(
            ObjectMask object,
            OutlineKernelParameters parameters,
            CheckedSupplier<EnergyStack, FeatureCalculationException> energyStack)
            throws FeatureCalculationException {
        Channel channel = energyStack.get().getChannel(energyIndex);

        OutlineKernelNeighborMatchValue kernelMatch =
                new OutlineKernelNeighborMatchValue(object, binaryVoxels(channel), parameters);
        return ApplyKernel.applyForCount(kernelMatch, object.voxels());
    }

    private BinaryVoxels<UnsignedByteBuffer> binaryVoxels(Channel channel)
            throws FeatureCalculationException {
        try {
            return BinaryVoxelsFactory.reuseByte(channel.voxels().asByte());

        } catch (IncorrectVoxelTypeException e) {
            throw new FeatureCalculationException(
                    String.format(
                            "energyStack channel %d has incorrect data type", getEnergyIndex()),
                    e);
        }
    }
}
