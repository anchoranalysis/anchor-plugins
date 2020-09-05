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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationOne;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.VoxelsWrapper;

public class Repeat extends BinarySegmentationOne {

    // START BEAN PROPERTIES
    /**
     * The maximum number of iterations. If the mask no longer has ON voxels after fewer iterations,
     * it will terminate earlier
     */
    @BeanField @Positive @Getter @Setter private int iterations = 10;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxels<UnsignedByteBuffer> segmentFromExistingSegmentation(
            VoxelsWrapper voxels,
            BinarySegmentationParameters params,
            Optional<ObjectMask> objectMask,
            BinarySegmentation sgmn)
            throws SegmentationFailedException {

        BinaryVoxels<UnsignedByteBuffer> outOld = null;

        int cnt = 0;
        while (cnt++ < iterations) {
            BinaryVoxels<UnsignedByteBuffer> outNew = sgmn.segment(voxels, params, objectMask);

            if (outNew == null) {
                return outOld;
            }

            outOld = outNew;

            // Increasingly tightens the obj-mask used for the segmentation
            objectMask =
                    Optional.of(
                            objectMask.isPresent()
                                    ? new ObjectMask(objectMask.get().boundingBox(), outNew)
                                    : new ObjectMask(outNew));
        }

        return outOld;
    }
}
