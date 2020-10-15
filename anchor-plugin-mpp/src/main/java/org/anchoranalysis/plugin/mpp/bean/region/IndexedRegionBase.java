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

package org.anchoranalysis.plugin.mpp.bean.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.mpp.bean.mark.MarkRegion;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.voxelized.VoxelizedMark;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;

/**
 * {@link MarkRegion} with a region and an index
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = false)
public abstract class IndexedRegionBase extends MarkRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int index = 0;

    @BeanField @Getter @Setter private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
    // END BEAN PROPERTIES

    @Override
    public String toString() {
        return String.format("regionID=%d,index=%d", regionID, index);
    }

    @Override
    public VoxelStatistics createStatisticsFor(VoxelizedMarkMemo memo, Dimensions dimensions)
            throws CreateException {
        return createStatisticsFor(memo.voxelized(), memo.getMark(), dimensions);
    }

    protected abstract VoxelStatistics createStatisticsFor(
            VoxelizedMark voxelized, Mark mark, Dimensions dimensions) throws CreateException;

    protected VoxelStatistics sliceStatisticsForRegion(VoxelizedMark voxelized, int z) {
        return voxelized.statisticsFor(index, regionID, z);
    }

    protected BoundingBox boundingBoxForRegion(VoxelizedMark vozelized) {
        return vozelized.boundingBox();
    }

    @Override
    public String uniqueName() {
        return String.format("%s_%d_%d", getClass().getCanonicalName(), index, regionID);
    }
}
