/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;

/**
 * Only takes pixels where indexNonZero has a nonzero pixel
 *
 * <p>This involves a trick where we count how many pixels exist in our mask and we take the highest
 * num-pixels to match this from our initial histogram
 */
@EqualsAndHashCode(callSuper = true)
public class AllSlicesMaskEverythingNonZero extends SelectSlicesWithIndexBase {

    @Override
    protected VoxelStatistics extractFromPxlMark(VoxelizedMark pm) throws CreateException {

        Histogram histIndex = histogramForAllSlices(pm, false);
        Histogram histNonZero = histogramForAllSlices(pm, true);

        long numNonZero =
                histNonZero.countThreshold(new RelationToConstant(new GreaterThanBean(), 0));

        return new VoxelStatisticsFromHistogram(histogramExtractedFromRight(histIndex, numNonZero));
    }

    private Histogram histogramForAllSlices(VoxelizedMark pm, boolean useNonZeroIndex)
            throws CreateException {
        try {
            return statisticsForAllSlices(pm, useNonZeroIndex).histogram();
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static Histogram histogramExtractedFromRight(Histogram histIndex, long numNonZero) {
        Histogram hOut = histIndex.duplicate();
        hOut = hOut.extractPixelsFromRight(numNonZero);
        return hOut;
    }
}
