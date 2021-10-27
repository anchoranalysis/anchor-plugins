/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.segment.binary;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.thresholder.slice.SliceThresholder;
import org.anchoranalysis.plugin.image.segment.thresholder.slice.SliceThresholderMask;
import org.anchoranalysis.plugin.image.segment.thresholder.slice.SliceThresholderWithoutMask;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Thresholds each voxels by comparing against another channel that has per-voxel thresholds
 *
 * <p>It sets an output voxel as high, if it is greater than or equal to the pixel in the threshold
 * channel
 *
 * @author Owen Feehan
 */
public class ThresholdAgainstChannel extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider channelThreshold;

    @BeanField @Getter @Setter private boolean clearOutsideMask = true;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxels<UnsignedByteBuffer> segment(
            VoxelsUntyped voxels, BinarySegmentationParameters params, Optional<ObjectMask> object)
            throws SegmentationFailedException {

        Voxels<?> voxelsIn = voxels.any();
        Voxels<UnsignedByteBuffer> voxelsOut = createOutputVoxels(voxels);

        BinaryValuesByte binaryValues = BinaryValuesByte.getDefault();

        SliceThresholder sliceThresholder = createThresholder(object, binaryValues);
        sliceThresholder.segmentAll(
                voxelsIn,
                createThresholdedVoxels(voxels.any().extent()),
                createOutputVoxels(voxels));

        return BinaryVoxelsFactory.reuseByte(voxelsOut, binaryValues.asInt());
    }

    private SliceThresholder createThresholder(
            Optional<ObjectMask> object, BinaryValuesByte binaryValues) {
        return object.map(
                        objectMask ->
                                (SliceThresholder)
                                        new SliceThresholderMask(
                                                clearOutsideMask, objectMask, binaryValues))
                .orElseGet(() -> new SliceThresholderWithoutMask(binaryValues));
    }

    private Voxels<?> createThresholdedVoxels(Extent voxelsExtent)
            throws SegmentationFailedException {

        Channel threshold;
        try {
            threshold = channelThreshold.get();
        } catch (ProvisionFailedException e) {
            throw new SegmentationFailedException(e);
        }

        Voxels<?> voxelsThresholded = threshold.voxels().any();

        if (!voxelsThresholded.extent().equals(voxelsExtent)) {
            throw new SegmentationFailedException(
                    "channelProviderThrshld is of different size to voxels");
        }

        return voxelsThresholded;
    }

    /**
     * Creates voxels to be outputted
     *
     * <p>If the input voxels are 8-bit we do it in place, otherwise, we create a new binary-voxels
     *
     * @param voxels
     * @return
     */
    private Voxels<UnsignedByteBuffer> createOutputVoxels(VoxelsUntyped voxels) {

        if (voxels.getVoxelDataType().equals(UnsignedByteVoxelType.INSTANCE)) {
            return voxels.asByte();
        } else {
            return VoxelsFactory.getUnsignedByte().createInitialized(voxels.any().extent());
        }
    }
}
