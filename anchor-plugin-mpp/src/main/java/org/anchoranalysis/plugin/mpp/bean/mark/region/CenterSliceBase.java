/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@EqualsAndHashCode(callSuper = true)
public abstract class CenterSliceBase extends IndexedRegionBase {

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {

        BoundingBox bbox = boundingBoxForRegion(pm);

        int zCenter = zCenterFromMark(mark, bbox);

        return createStatisticsForBBox(pm, dimensions, bbox, zCenter);
    }

    protected abstract VoxelStatistics createStatisticsForBBox(
            VoxelizedMark pm, ImageDimensions dimensions, BoundingBox bbox, int zCenter);

    private static int zCenterFromMark(Mark markUncasted, BoundingBox bbox) throws CreateException {
        if (!(markUncasted instanceof MarkAbstractPosition)) {
            throw new CreateException(
                    "Only marks that inherit from MarkAbstractPosition are supported");
        }

        MarkAbstractPosition mark = (MarkAbstractPosition) markUncasted;

        return (int) Math.round(mark.getPos().getZ()) - bbox.cornerMin().getZ();
    }
}
