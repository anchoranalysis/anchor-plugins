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
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationUnary;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Repeats a binary segmentation operation for a specified number of iterations.
 */
public class Repeat extends BinarySegmentationUnary {

    // START BEAN PROPERTIES
    /**
     * The maximum number of iterations.
     * 
     * <p>If the mask no longer has <i>on</i> voxels after fewer iterations, it will terminate earlier.
     */
    @BeanField @Positive @Getter @Setter private int iterations = 10;
    // END BEAN PROPERTIES

    // The segmentFromExistingSegmentation method is overridden, so we don't add a doc-string here
    @Override
    public BinaryVoxels<UnsignedByteBuffer> segmentFromExistingSegmentation(
            VoxelsUntyped voxels,
            BinarySegmentationParameters parameters,
            Optional<ObjectMask> objectMask,
            BinarySegmentation segment)
            throws SegmentationFailedException {

        BinaryVoxels<UnsignedByteBuffer> outOld = null;

        int count = 0;
        while (count++ < iterations) {
            BinaryVoxels<UnsignedByteBuffer> outNew =
                    segment.segment(voxels, parameters, objectMask);

            if (outNew == null) {
                return outOld;
            }

            outOld = outNew;

            // Increasingly tightens the object-mask used for the segmentation
            objectMask =
                    Optional.of(
                            objectMask.isPresent()
                                    ? new ObjectMask(objectMask.get().boundingBox(), outNew)
                                    : new ObjectMask(outNew));
        }

        return outOld;
    }
}
