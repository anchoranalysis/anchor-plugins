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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Performs a sequence of succesive segmentations
 *
 * @author Owen Feehan
 */
public class Sequence extends BinarySegmentation {

    // START BEAN PROPERTIES
    /**
     * A list of binary-segmentations that are applied successively (the output of the predecessor
     * becomes the input of the sucecssor)
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<BinarySegmentation> list = new ArrayList<>();

    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (list.isEmpty()) {
            throw new BeanMisconfiguredException("At least one item is required in listSgmn");
        }
    }

    @Override
    public BinaryVoxels<UnsignedByteBuffer> segment(
            VoxelsUntyped voxels,
            BinarySegmentationParameters parameters,
            Optional<ObjectMask> objectMask)
            throws SegmentationFailedException {

        BinaryVoxels<UnsignedByteBuffer> out = null;

        // A bounding-box capturing what part of the scene is being segmented
        BoundingBox box =
                objectMask
                        .map(ObjectMask::boundingBox)
                        .orElseGet(() -> new BoundingBox(voxels.extent()));

        // A mask that evolves as we move through each segmentation to be increasingly smaller.
        Optional<ObjectMask> evolvingMask = objectMask;
        for (BinarySegmentation segment : list) {

            BinaryVoxels<UnsignedByteBuffer> outNew =
                    segment.segment(voxels, parameters, evolvingMask);

            out = outNew;
            evolvingMask = Optional.of(new ObjectMask(box, outNew));
        }
        return out;
    }
}
