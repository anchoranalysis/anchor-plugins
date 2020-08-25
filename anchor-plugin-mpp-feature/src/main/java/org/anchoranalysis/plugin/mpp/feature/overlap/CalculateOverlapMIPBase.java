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

package org.anchoranalysis.plugin.mpp.feature.overlap;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.feature.cache.calculate.FeatureCalculation;
import org.anchoranalysis.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.mpp.mark.voxelized.VoxelizedMark;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.overlap.MaxIntensityProjectionPair;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateOverlapMIPBase
        extends FeatureCalculation<Double, FeatureInputPairMemo> {

    private final int regionID;

    @Override
    protected Double execute(FeatureInputPairMemo params) {

        VoxelizedMarkMemo mark1 = params.getObj1();
        VoxelizedMarkMemo mark2 = params.getObj2();

        VoxelizedMark voxelized1 = mark1.voxelized();
        VoxelizedMark voxelized2 = mark2.voxelized();

        if (!voxelized1.boundingBoxFlattened().intersection().existsWith(voxelized2.boundingBoxFlattened())) {
            return 0.0;
        }

        MaxIntensityProjectionPair pair =
                new MaxIntensityProjectionPair(
                        voxelized1.voxelsMaximumIntensityProjection(),
                        voxelized2.voxelsMaximumIntensityProjection(),
                        regionMembershipForMark(mark1),
                        regionMembershipForMark(mark2));

        double overlap = pair.countIntersectingVoxels();

        return calculateOverlapResult(overlap, pair);
    }

    protected abstract Double calculateOverlapResult(
            double overlap, MaxIntensityProjectionPair pair);

    private RegionMembershipWithFlags regionMembershipForMark(VoxelizedMarkMemo mark) {
        return mark.getRegionMap().membershipWithFlagsForIndex(regionID);
    }
}
