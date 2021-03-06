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

package org.anchoranalysis.plugin.mpp.bean.segment.binary;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationThreshold;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.bound.MarkBounds;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;
import org.anchoranalysis.spatial.Extent;

// Performs a thresholding that accepts only channel values with intensities
//   greater than the minimum bound
public class SegmentThresholdAboveMinBound extends BinarySegmentation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean suppress3D = false;

    @BeanField @Getter @Setter private MarkBounds markBounds;
    // END BEAN PROPERTIES

    private BinarySegmentationThreshold delegate = new BinarySegmentationThreshold();

    @Override
    public BinaryVoxels<UnsignedByteBuffer> segment(
            VoxelsWrapper voxels,
            BinarySegmentationParameters params,
            Optional<ObjectMask> objectMask)
            throws SegmentationFailedException {

        setUpDelegate(voxels.any().extent(), params.getResolution());

        return delegate.segment(voxels, params, objectMask);
    }

    private void setUpDelegate(Extent extent, Optional<Resolution> resolution) {
        double minBound = markBounds.getMinResolved(resolution, extent.z() > 1 && !suppress3D);

        int threshold = (int) Math.floor(minBound);

        CalculateLevel calculateLevel = new Constant(threshold);

        ThresholderGlobal thresholder = new ThresholderGlobal();
        thresholder.setCalculateLevel(calculateLevel);

        delegate.setThresholder(thresholder);
    }
}
