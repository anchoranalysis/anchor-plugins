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

package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.datatype.UnsignedByte;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;

// Performs a threshold on each pixel, by comparing the pixel value to another channel
//  It sets a pixel as high, if it is greater than or equal to the pixel in the other "Thrshld"
// channel
public class SgmnThrshldAgainstChnl extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnlThreshold;

    @BeanField @Getter @Setter private boolean clearOutsideMask = true;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxels<ByteBuffer> segment(
            VoxelsWrapper voxels, BinarySegmentationParameters params, Optional<ObjectMask> object)
            throws SegmentationFailedException {

        Voxels<?> voxelsIn = voxels.any();
        Voxels<ByteBuffer> voxelsOut = createOutputVoxels(voxels);

        BinaryValuesByte bvb = BinaryValuesByte.getDefault();

        SliceThresholder sliceThresholder = createThresholder(object, bvb);
        sliceThresholder.sgmnAll(
                voxelsIn,
                createThresholdedVoxels(voxels.any().extent()),
                createOutputVoxels(voxels));

        return BinaryVoxelsFactory.reuseByte(voxelsOut, bvb.createInt());
    }

    private SliceThresholder createThresholder(Optional<ObjectMask> object, BinaryValuesByte bvb) {
        return object.map(
                        objectMask ->
                                (SliceThresholder)
                                        new SliceThresholderMask(clearOutsideMask, objectMask, bvb))
                .orElseGet(() -> new SliceThresholderWithoutMask(bvb));
    }

    private Voxels<?> createThresholdedVoxels(Extent voxelsExtent)
            throws SegmentationFailedException {

        Channel threshold;
        try {
            threshold = chnlThreshold.create();
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }

        Voxels<?> voxelsThresholded = threshold.voxels().any();

        if (!voxelsThresholded.extent().equals(voxelsExtent)) {
            throw new SegmentationFailedException(
                    "chnlProviderThrshld is of different size to voxels");
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
    private Voxels<ByteBuffer> createOutputVoxels(VoxelsWrapper voxels) {

        if (voxels.getVoxelDataType().equals(UnsignedByte.INSTANCE)) {
            return voxels.asByte();
        } else {
            return VoxelsFactory.getByte().createInitialized(voxels.any().extent());
        }
    }
}
