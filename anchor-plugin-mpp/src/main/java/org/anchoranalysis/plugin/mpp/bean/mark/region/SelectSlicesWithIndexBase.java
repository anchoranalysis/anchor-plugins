/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@EqualsAndHashCode(callSuper = false)
public abstract class SelectSlicesWithIndexBase extends SelectSlicesBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int indexNonZero = 0;
    // END BEAN PROPERTIES

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {
        return extractFromPxlMark(pm);
    }

    protected abstract VoxelStatistics extractFromPxlMark(VoxelizedMark pm) throws CreateException;

    protected VoxelStatistics statisticsForAllSlices(VoxelizedMark pm, boolean useNonZeroIndex) {
        return pm.statisticsForAllSlicesMaskSlice(
                useNonZeroIndex ? indexNonZero : getIndex(), getRegionID(), indexNonZero);
    }

    @Override
    public String uniqueName() {
        return String.format("%s_%d", super.uniqueName(), indexNonZero);
    }

    @Override
    public String toString() {
        return String.format(
                "regionID=%d,index=%d,indexNonZero=%d", getRegionID(), getIndex(), indexNonZero);
    }
}
