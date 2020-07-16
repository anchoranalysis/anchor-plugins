/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@EqualsAndHashCode(callSuper = false)
public class GreatestAreaSlice extends IndexedRegionBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {

        BoundingBox bbox = boundingBoxForRegion(pm);

        long maxArea = -1;
        VoxelStatistics psMax = null;
        for (int z = 0; z < bbox.extent().getZ(); z++) {

            VoxelStatistics ps = sliceStatisticsForRegion(pm, z);
            long num = ps.countThreshold(threshold);

            if (num > maxArea) {
                psMax = ps;
                maxArea = num;
            }
        }

        assert (psMax != null);
        return psMax;
    }

    @Override
    public String uniqueName() {
        return super.uniqueName() + "_" + threshold.uniqueName();
    }
}
