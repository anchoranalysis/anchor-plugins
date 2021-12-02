/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.image.feature.input.FeatureInputHistogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.mpp.bean.mark.MarkRegion;
import org.anchoranalysis.mpp.feature.input.FeatureInputSingleMemo;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHistogramInputFromMemo
        extends CalculationPart<FeatureInputHistogram, FeatureInputSingleMemo> {

    private final MarkRegion pixelList;
    private final boolean excludeZero;

    @Override
    protected FeatureInputHistogram execute(FeatureInputSingleMemo input)
            throws FeatureCalculationException {

        try {
            VoxelStatistics stats =
                    pixelList.createStatisticsFor(
                            input.getPxlPartMemo(), input.dimensionsRequired());

            Histogram histogram = maybeExcludeZeros(stats.histogram());

            return new FeatureInputHistogram(histogram, input.getResolutionOptional());
        } catch (CreateException | OperationFailedException e) {
            throw new FeatureCalculationException(e);
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
