/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

/** Gets all slices where indexNonZero has at least one non-zero pixel in that slice */
@EqualsAndHashCode(callSuper = true)
public class AllSlicesMaskNonZero extends SelectSlicesWithIndexBase {

    @Override
    protected VoxelStatistics extractFromPxlMark(VoxelizedMark pm) throws CreateException {
        return statisticsForAllSlices(pm, false);
    }
}
