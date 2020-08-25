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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import java.util.function.LongBinaryOperator;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;
import org.anchoranalysis.plugin.mpp.feature.overlap.CalculateOverlapMask;

public abstract class OverlapMaskBase extends FeaturePairMemoSingleRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int maskValue = 255;

    @BeanField @Getter @Setter private int energyIndex = 0;
    // END BEAN PROPERTIES

    protected double overlapWithGlobalMask(SessionInput<FeatureInputPairMemo> params)
            throws FeatureCalculationException {
        return params.calc(
                new CalculateOverlapMask(getRegionID(), getEnergyIndex(), (byte) getMaskValue()));
    }

    @Override
    protected double overlappingNumVoxels(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {
        return input.calc(new CalculateOverlapMask(getRegionID(), energyIndex, (byte) maskValue));
    }

    protected double volumeAgg(
            VoxelizedMarkMemo obj1,
            VoxelizedMarkMemo obj2,
            int regionID,
            RelationBean relationToThreshold,
            LongBinaryOperator statFunc) {
        return volumeStatistic(obj1, obj2, regionID, relationToThreshold, statFunc);
    }

    protected double volumeStatistic(
            VoxelizedMarkMemo obj1,
            VoxelizedMarkMemo obj2,
            int regionID,
            RelationBean relationToThreshold,
            LongBinaryOperator statFunc) {

        long size1 = sizeForObject(obj1, regionID, relationToThreshold);
        long size2 = sizeForObject(obj2, regionID, relationToThreshold);
        return statFunc.applyAsLong(size1, size2);
    }

    private long sizeForObject(VoxelizedMarkMemo obj, int regionID, RelationBean relationToThreshold) {
        VoxelStatistics pxlStats = obj.voxelized().statisticsForAllSlices(energyIndex, regionID);
        return pxlStats.countThreshold(new RelationToConstant(relationToThreshold, maskValue));
    }
}
