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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

// Thresholds an image, then creates an object out of each
//   connected set of pixels, and then performs another threshold
//   seperately on each object, setting all pixels not contained
//   within the object to black
// WE SHOULD DELETE THIS
public class SgmnObject extends BinarySegmentation {

    // START BEANS
    /** The Thresholder applied on the whole image */
    @BeanField @Getter @Setter private BinarySegmentation imageSgmn;

    /** The Thresholder applied on the local object */
    @BeanField @Getter @Setter private BinarySegmentation objectSgmn;

    @BeanField @Positive @Getter @Setter private int minNumPixelsImageSgmn = 100;
    // END BEANS

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBoxIn,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask)
            throws SegmentationFailedException {

        if (mask.isPresent()) {
            throw new SegmentationFailedException("Masks are not supported on this operation");
        }

        // Keep a copy of the original unchanged
        VoxelBox<?> orig = voxelBoxIn.any().duplicate();

        imageSgmn.sgmn(voxelBoxIn, params, Optional.empty());

        VoxelBox<ByteBuffer> out = voxelBoxIn.asByte();

        sgmnByObj(
                new BinaryVoxelBoxByte(out, BinaryValues.getDefault()),
                new VoxelBoxWrapper(orig),
                params);

        return new BinaryVoxelBoxByte(out, BinaryValues.getDefault());
    }

    private void sgmnByObj(
            BinaryVoxelBox<ByteBuffer> voxelBox,
            VoxelBoxWrapper orig,
            BinarySegmentationParameters params)
            throws SegmentationFailedException {

        for (ObjectMask obj : objectsFromVoxelBox(voxelBox)) {

            if (!obj.numPixelsLessThan(minNumPixelsImageSgmn)) {

                BinaryVoxelBox<ByteBuffer> out = objectSgmn.sgmn(orig, params, Optional.of(obj));

                if (out == null) {
                    continue;
                }

                out.copyPixelsToCheckMask(
                        new BoundingBox(obj.getBoundingBox().extent()),
                        voxelBox.getVoxelBox(),
                        obj.getBoundingBox(),
                        obj.getVoxelBox(),
                        obj.getBinaryValuesByte());

            } else {
                voxelBox.setPixelsCheckMaskOff(obj);
            }
        }
    }

    private static ObjectCollection objectsFromVoxelBox(BinaryVoxelBox<ByteBuffer> buffer)
            throws SegmentationFailedException {
        try {
            CreateFromConnectedComponentsFactory omcCreator =
                    new CreateFromConnectedComponentsFactory();
            return omcCreator.createConnectedComponents(buffer.duplicate());
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
