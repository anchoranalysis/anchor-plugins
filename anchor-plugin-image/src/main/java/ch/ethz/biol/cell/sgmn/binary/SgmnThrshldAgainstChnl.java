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
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Performs a threshold on each pixel, by comparing the pixel value to another channel
//  It sets a pixel as high, if it is greater than or equal to the pixel in the other "Thrshld"
// channel
public class SgmnThrshldAgainstChnl extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnlThreshold;

    @BeanField @Getter @Setter private boolean clearOutsideMask = true;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> object)
            throws SegmentationFailedException {

        VoxelBox<?> voxelBoxIn = voxelBox.any();
        VoxelBox<ByteBuffer> voxelBoxOut = createOutputChnl(voxelBox);

        BinaryValuesByte bvb = BinaryValuesByte.getDefault();

        SliceThresholder sliceThresholder = createThresholder(object, bvb);
        sliceThresholder.sgmnAll(
                voxelBoxIn,
                createThresholdedVoxelBox(voxelBox.any().extent()),
                createOutputChnl(voxelBox));

        return new BinaryVoxelBoxByte(voxelBoxOut, bvb.createInt());
    }

    private SliceThresholder createThresholder(Optional<ObjectMask> object, BinaryValuesByte bvb) {
        return object.map(
                        objectMask ->
                                (SliceThresholder)
                                        new SliceThresholderMask(clearOutsideMask, objectMask, bvb))
                .orElseGet(() -> new SliceThresholderWithoutMask(bvb));
    }

    private VoxelBox<?> createThresholdedVoxelBox(Extent voxelBoxExtent)
            throws SegmentationFailedException {

        Channel threshold;
        try {
            threshold = chnlThreshold.create();
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }

        VoxelBox<?> vbThrshld = threshold.getVoxelBox().any();

        if (!vbThrshld.extent().equals(voxelBoxExtent)) {
            throw new SegmentationFailedException(
                    "chnlProviderThrshld is of different size to voxelBox");
        }

        return vbThrshld;
    }

    // If the input voxel-box is 8-bit we do it in place
    // Otherwise, we create a new binary voxelbox buffer
    private VoxelBox<ByteBuffer> createOutputChnl(VoxelBoxWrapper voxelBox) {

        if (voxelBox.getVoxelDataType().equals(VoxelDataTypeUnsignedByte.INSTANCE)) {
            return voxelBox.asByte();
        } else {
            return VoxelBoxFactory.getByte().create(voxelBox.any().extent());
        }
    }
}
