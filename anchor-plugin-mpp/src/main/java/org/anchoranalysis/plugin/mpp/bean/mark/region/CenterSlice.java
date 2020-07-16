/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsCombined;

@EqualsAndHashCode(callSuper = true)
public class CenterSlice extends CenterSliceBase {

    @Override
    protected VoxelStatistics createStatisticsForBBox(
            VoxelizedMark pm, ImageDimensions dimensions, BoundingBox bbox, int zCenter) {

        if (zCenter < 0 || zCenter >= bbox.extent().getZ()) {
            return new VoxelStatisticsCombined();
        }

        return sliceStatisticsForRegion(pm, zCenter);
    }
}
