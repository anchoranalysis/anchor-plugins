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
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationOne;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.VoxelsWrapper;

public class SgmnInv extends BinarySegmentationOne {

    @Override
    public BinaryVoxels<ByteBuffer> sgmnFromSgmn(
            VoxelsWrapper voxels,
            BinarySegmentationParameters params,
            Optional<ObjectMask> objectMask,
            BinarySegmentation sgmn)
            throws SegmentationFailedException {

        BinaryVoxels<ByteBuffer> voxelsSegmented = sgmn.segment(voxels, params, objectMask);
        invertVoxels(voxelsSegmented);
        return voxelsSegmented;
    }

    private void invertVoxels(BinaryVoxels<ByteBuffer> voxels) {

        BinaryValuesByte bv = voxels.binaryValues().createByte();

        int volumeXY = voxels.extent().volumeXY();

        // We invert each item in the voxels
        for (int z = 0; z < voxels.extent().z(); z++) {

            ByteBuffer bb = voxels.sliceBuffer(z);
            for (int index = 0; index < volumeXY; index++) {

                byte val = bb.get(index);

                if (val == bv.getOnByte()) {
                    bb.put(index, bv.getOffByte());
                } else if (val == bv.getOffByte()) {
                    bb.put(index, bv.getOnByte());
                } else {
                    assert false;
                }
            }
        }
    }
}
