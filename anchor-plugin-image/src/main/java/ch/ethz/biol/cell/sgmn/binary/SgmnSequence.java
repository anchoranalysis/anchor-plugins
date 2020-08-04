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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnSequence extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter
    private List<BinarySegmentation> listSgmn = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (listSgmn.isEmpty()) {
            throw new BeanMisconfiguredException("At least one item is required in listSgmn");
        }
    }

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> objectMask)
            throws SegmentationFailedException {

        BinaryVoxelBox<ByteBuffer> out = null;

        // A bounding-box capturing what part of the scene is being segmented
        BoundingBox bbox =
                objectMask.map(ObjectMask::getBoundingBox)
                        .orElseGet(() -> new BoundingBox(voxelBox.any().extent()));

        // A mask that evolves as we move through each segmentation to be increasingly smaller.
        Optional<ObjectMask> evolvingMask = objectMask;
        for (BinarySegmentation sgmn : listSgmn) {

            BinaryVoxelBox<ByteBuffer> outNew = sgmn.sgmn(voxelBox, params, evolvingMask);

            out = outNew;
            evolvingMask = Optional.of(new ObjectMask(bbox, outNew));
        }
        return out;
    }
}
