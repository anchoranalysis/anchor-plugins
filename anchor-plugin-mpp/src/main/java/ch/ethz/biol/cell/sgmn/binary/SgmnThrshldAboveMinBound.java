/*-
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.anchor.mpp.bean.bound.MarkBounds;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationThreshold;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;

// Performs a thresholding that accepts only channel values with intensities
//   greater than the minimum bound
public class SgmnThrshldAboveMinBound extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField private boolean suppress3D = false;

    @BeanField private MarkBounds markBounds;
    // END BEAN PROPERTIES

    private BinarySegmentationThreshold delegate = new BinarySegmentationThreshold();

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask)
            throws SegmentationFailedException {

        setUpDelegate(
                voxelBox.any().extent(),
                params.getRes()
                        .orElseThrow(
                                () ->
                                        new SegmentationFailedException(
                                                "Image-resolution is required but missing")));

        return delegate.sgmn(voxelBox, params, mask);
    }

    private void setUpDelegate(Extent e, ImageResolution res) {
        double minBound = markBounds.getMinResolved(res, e.getZ() > 1 && !suppress3D);

        int threshold = (int) Math.floor(minBound);

        CalculateLevel calculateLevel = new Constant(threshold);

        ThresholderGlobal thresholder = new ThresholderGlobal();
        thresholder.setCalculateLevel(calculateLevel);

        delegate.setThresholder(thresholder);
    }

    public boolean isSuppress3D() {
        return suppress3D;
    }

    public void setSuppress3D(boolean suppress3d) {
        suppress3D = suppress3d;
    }

    public MarkBounds getMarkBounds() {
        return markBounds;
    }

    public void setMarkBounds(MarkBounds markBounds) {
        this.markBounds = markBounds;
    }
}
