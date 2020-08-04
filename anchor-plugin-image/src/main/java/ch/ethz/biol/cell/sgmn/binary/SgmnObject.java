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
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;

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
    public BinaryVoxels<ByteBuffer> segment(
            VoxelsWrapper voxelsIn,
            BinarySegmentationParameters params,
            Optional<ObjectMask> objectMask)
            throws SegmentationFailedException {

        if (objectMask.isPresent()) {
            throw new SegmentationFailedException("Masks are not supported on this operation");
        }

        // Keep a copy of the original unchanged
        Voxels<?> orig = voxelsIn.any().duplicate();

        imageSgmn.segment(voxelsIn, params, Optional.empty());

        BinaryVoxels<ByteBuffer> out = BinaryVoxelsFactory.reuseByte(voxelsIn.asByte());
        segmentByObject(
                out,
                new VoxelsWrapper(orig),
                params);

        return out;
    }

    private void segmentByObject(
            BinaryVoxels<ByteBuffer> voxels,
            VoxelsWrapper orig,
            BinarySegmentationParameters params)
            throws SegmentationFailedException {

        for (ObjectMask objects : objectsFromVoxels(voxels)) {

            if (!objects.numPixelsLessThan(minNumPixelsImageSgmn)) {

                BinaryVoxels<ByteBuffer> out = objectSgmn.segment(orig, params, Optional.of(objects));

                if (out == null) {
                    continue;
                }

                out.copyPixelsToCheckMask(
                        new BoundingBox(objects.getBoundingBox().extent()),
                        voxels.getVoxels(),
                        objects.getBoundingBox(),
                        objects.getVoxels(),
                        objects.getBinaryValuesByte());

            } else {
                voxels.setPixelsCheckMaskOff(objects);
            }
        }
    }

    private static ObjectCollection objectsFromVoxels(BinaryVoxels<ByteBuffer> voxels)
            throws SegmentationFailedException {
        try {
            CreateFromConnectedComponentsFactory omcCreator =
                    new CreateFromConnectedComponentsFactory();
            return omcCreator.createConnectedComponents(voxels.duplicate());
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
