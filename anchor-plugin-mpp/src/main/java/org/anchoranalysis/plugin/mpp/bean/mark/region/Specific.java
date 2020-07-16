/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@EqualsAndHashCode(callSuper = true)
public class Specific extends SelectSlicesBase {

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {
        if (getSliceID() == -1) {
            return pm.statisticsForAllSlices(getIndex(), getRegionID());
        } else {
            return this.sliceStatisticsForRegion(pm, getSliceID());
        }
    }
}
