/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.MarkRegion;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHistogramInputFromMemo
        extends FeatureCalculation<FeatureInputHistogram, FeatureInputSingleMemo> {

    private final MarkRegion pixelList;
    private final boolean excludeZero;

    @Override
    protected FeatureInputHistogram execute(FeatureInputSingleMemo input)
            throws FeatureCalcException {

        try {
            VoxelStatistics stats =
                    pixelList.createStatisticsFor(
                            input.getPxlPartMemo(), input.getDimensionsRequired());

            Histogram hist = maybeExcludeZeros(stats.histogram());

            return new FeatureInputHistogram(hist, input.getResOptional());
        } catch (CreateException | OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    private Histogram maybeExcludeZeros(Histogram histogramFromStats) {
        if (excludeZero) {
            Histogram out = histogramFromStats.duplicate();
            out.removeBelowThreshold(1);
            return out;
        } else {
            return histogramFromStats;
        }
    }
}
