/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.MarkRegion;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class Thresholded extends MarkRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkRegion region;

    @BeanField @Getter @Setter private RelationToThreshold threshold;
    // END BEAN PROPERTIES

    @Override
    public VoxelStatistics createStatisticsFor(VoxelizedMarkMemo memo, ImageDimensions dimensions)
            throws CreateException {
        return region.createStatisticsFor(memo, dimensions).threshold(threshold);
    }

    @Override
    public String uniqueName() {
        return String.format(
                "%s_%s_%s",
                Thresholded.class.getCanonicalName(), region.uniqueName(), threshold.uniqueName());
    }
}
