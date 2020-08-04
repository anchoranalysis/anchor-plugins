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

package org.anchoranalysis.plugin.image.feature.bean.object.single.nrg;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3NeighborMatchValue;

/**
 * Calculates the number of voxels on the object that have a neighbor (according to a binary-mask on
 * an nrg-channel)
 *
 * <p>The nrg-channel should be a binary-channel (with 255 high, and 0 low) showing all possible
 * neighbor voxels
 *
 * @author Owen Feehan
 */
public class NumberNeighboringVoxels extends SpecificNRGChannelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES

    @Override
    protected double calcWithChannel(ObjectMask object, Channel chnl)
            throws FeatureCalculationException {

        OutlineKernel3NeighborMatchValue kernelMatch =
                new OutlineKernel3NeighborMatchValue(
                        outsideAtThreshold, do3D, object, binaryVoxels(chnl), ignoreAtThreshold);
        return ApplyKernel.applyForCount(kernelMatch, object.voxels());
    }

    private BinaryVoxels<ByteBuffer> binaryVoxels(Channel chnl)
            throws FeatureCalculationException {
        try {
            return BinaryVoxelsFactory.reuseByte(chnl.voxels().asByte());

        } catch (IncorrectVoxelDataTypeException e) {
            throw new FeatureCalculationException(
                    String.format("nrgStack channel %d has incorrect data type", getNrgIndex()), e);
        }
    }
}
