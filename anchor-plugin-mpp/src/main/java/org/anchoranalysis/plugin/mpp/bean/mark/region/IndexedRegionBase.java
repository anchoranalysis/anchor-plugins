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
