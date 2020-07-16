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

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

// Returns the maximum area of each slice
public final class MaxSliceArea extends FeatureSingleMemoRegion {

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        VoxelizedMark pm = input.get().getPxlPartMemo().voxelized();

        double maxSliceSizeVoxels = calcMaxSliceSize(pm);

        double retVal = resolveArea(maxSliceSizeVoxels, input.get().getResOptional());

        getLogger().messageLogger().logFormatted("MaxSliceArea = %f\n", retVal);
        return retVal;
    }

    private long calcMaxSliceSize(VoxelizedMark pm) {

        long max = 0;
        for (int z = 0; z < pm.getVoxelBox().extent().getZ(); z++) {

            VoxelStatistics pxlStats = pm.statisticsFor(0, getRegionID(), z);

            long size = pxlStats.size();

            if (size > max) {
                max = size;
            }
        }
        return max;
    }
}
