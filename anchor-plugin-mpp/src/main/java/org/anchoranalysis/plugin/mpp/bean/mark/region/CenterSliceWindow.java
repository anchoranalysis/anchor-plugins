/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
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
            VoxelizedMark pm, ImageDimensions dimensions, BoundingBox bbox, int zCenter) {

        // If our z-center is off scene we bring it to the closest value, but we guard against the
        // case where the top of the mark is also off scene
        if (zCenter < 0) {
            zCenter = 0;
        }

        if (zCenter >= bbox.extent().getZ()) {
            zCenter = bbox.extent().getZ() - 1;
        }
        assert (zCenter >= 0);
        assert (zCenter < bbox.extent().getZ());

        // Early exit if the windowSize is 0
        if (windowSize == 0) {
            return sliceStatisticsForRegion(pm, zCenter);
        }

        int zLow = Math.max(zCenter - windowSize, 0);
        int zHigh = Math.min(zCenter + windowSize, bbox.extent().getZ() - 1);

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
