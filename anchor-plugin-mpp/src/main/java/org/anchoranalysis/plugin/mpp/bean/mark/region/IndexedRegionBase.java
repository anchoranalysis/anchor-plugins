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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkRegion;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

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
    public VoxelStatistics createStatisticsFor(VoxelizedMarkMemo memo, ImageDimensions dimensions)
            throws CreateException {
        return createStatisticsFor(memo.voxelized(), memo.getMark(), dimensions);
    }

    protected abstract VoxelStatistics createStatisticsFor(
            VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException;

    protected VoxelStatistics sliceStatisticsForRegion(VoxelizedMark pm, int z) {
        return pm.statisticsFor(index, regionID, z);
    }

    protected BoundingBox boundingBoxForRegion(VoxelizedMark pm) {
        return pm.getBoundingBox();
    }

    @Override
    public String uniqueName() {
        return String.format("%s_%d_%d", getClass().getCanonicalName(), index, regionID);
    }
}
