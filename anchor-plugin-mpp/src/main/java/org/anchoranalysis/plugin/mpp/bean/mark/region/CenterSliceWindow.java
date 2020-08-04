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

package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.voxelized.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsCombined;

/**
 * Like {#link ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.CenterSlice} but considers more
 * than one slice, specifically centerSlice+- windowSize
 *
 * <p>So total size = 2*windowSize + 1 (clipped to the bounding box)
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = true)
public class CenterSliceWindow extends CenterSliceBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int windowSize = 0;
    // END BEAN PROPERTIES

    @Override
    protected VoxelStatistics createStatisticsForBBox(
            VoxelizedMark pm, ImageDimensions dimensions, BoundingBox box, int zCenter) {

        // If our z-center is off scene we bring it to the closest value, but we guard against the
        // case where the top of the mark is also off scene
        if (zCenter < 0) {
            zCenter = 0;
        }

        if (zCenter >= box.extent().z()) {
            zCenter = box.extent().z() - 1;
        }
        assert (zCenter >= 0);
        assert (zCenter < box.extent().z());

        // Early exit if the windowSize is 0
        if (windowSize == 0) {
            return sliceStatisticsForRegion(pm, zCenter);
        }

        int zLow = Math.max(zCenter - windowSize, 0);
        int zHigh = Math.min(zCenter + windowSize, box.extent().z() - 1);

        VoxelStatisticsCombined out = new VoxelStatisticsCombined();
        for (int z = zLow; z <= zHigh; z++) {
            out.add(sliceStatisticsForRegion(pm, z));
        }
        return out;
    }

    @Override
    public String uniqueName() {
        return super.uniqueName() + "_" + windowSize;
    }
}
