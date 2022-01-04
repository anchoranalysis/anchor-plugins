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
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationUnary;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Performs a segmentation and inverts and <i>on</i> and <i>off</i> bytes
 *
 * @author Owen Feehan
 */
public class Invert extends BinarySegmentationUnary {

    @Override
    public BinaryVoxels<UnsignedByteBuffer> segmentFromExistingSegmentation(
            VoxelsUntyped voxels,
            BinarySegmentationParameters parameters,
            Optional<ObjectMask> objectMask,
            BinarySegmentation segment)
            throws SegmentationFailedException {

        BinaryVoxels<UnsignedByteBuffer> voxelsSegmented =
                segment.segment(voxels, parameters, objectMask);
        invertVoxels(voxelsSegmented);
        return voxelsSegmented;
    }

    private void invertVoxels(BinaryVoxels<UnsignedByteBuffer> voxels) {

        BinaryValuesByte bv = voxels.binaryValues().asByte();

        int volumeXY = voxels.extent().areaXY();

        // We invert each item in the voxels
        for (int z = 0; z < voxels.extent().z(); z++) {

            UnsignedByteBuffer buffer = voxels.sliceBuffer(z);
            for (int index = 0; index < volumeXY; index++) {

                byte val = buffer.getRaw(index);

                if (val == bv.getOn()) {
                    buffer.putRaw(index, bv.getOff());
                } else if (val == bv.getOff()) {
                    buffer.putRaw(index, bv.getOn());
                } else {
                    assert false;
                }
            }
        }
    }
}
