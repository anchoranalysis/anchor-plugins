/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.region;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;
import org.anchoranalysis.mpp.mark.voxelized.VoxelizedMark;

/**
 * Only takes pixels where indexNonZero has a nonzero pixel
 *
 * <p>This involves a trick where we count how many pixels exist in our object-mask and we take the
 * highest number pixels to match this from our initial histogram
 */
@EqualsAndHashCode(callSuper = true)
public class AllSlicesMaskEverythingNonZero extends SelectSlicesWithIndexBase {

    @Override
    protected VoxelStatistics extractFromVoxelized(VoxelizedMark mark) throws CreateException {

        Histogram index = histogramForAllSlices(mark, false);
        Histogram nonZero = histogramForAllSlices(mark, true);

        long numNonZero =
                nonZero.countThreshold(new RelationToConstant(new GreaterThanBean(), 0));

        return new VoxelStatisticsFromHistogram(histogramExtractedFromRight(index, numNonZero));
    }

    private Histogram histogramForAllSlices(VoxelizedMark mark, boolean useNonZeroIndex)
            throws CreateException {
        try {
            return statisticsForAllSlices(mark, useNonZeroIndex).histogram();
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static Histogram histogramExtractedFromRight(Histogram index, long numNonZero) {
        Histogram out = index.duplicate();
        out = out.extractValuesFromRight(numNonZero);
        return out;
    }
}
