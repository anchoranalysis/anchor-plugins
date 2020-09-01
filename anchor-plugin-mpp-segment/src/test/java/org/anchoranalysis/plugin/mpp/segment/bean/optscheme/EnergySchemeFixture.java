/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment.bean.optscheme;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.feature.addcriteria.BoundingBoxIntersection;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergyScheme;
import org.anchoranalysis.mpp.feature.energy.scheme.EnergySchemeWithSharedFeatures;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.ind.Size;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap.OverlapNumVoxels;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.test.LoggingFixture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnergySchemeFixture {

    /**
     * An energy-scheme that is rewarded by larger marks but with a penalty for overlap.
     *
     * <p><div> Specifically it is: <code>
     *  sum(voxels across each mark) - weight * sum(overlapping voxels regions between marks)
     * </code> </div>
     *
     * @param weightOverlap a positive integer indicating how much to penalize the overlapping
     *     voxels by, the higher the greater the penalty.
     * @return
     * @throws CreateException
     */
    public static EnergySchemeWithSharedFeatures sizeMinusWeightedOverlap(double weightOverlap)
            throws CreateException {
        return new EnergySchemeWithSharedFeatures(
                createEnergyScheme(weightOverlap),
                new SharedFeatureMulti(),
                LoggingFixture.suppressedLogErrorReporter());
    }

    private static EnergyScheme createEnergyScheme(double weightOverlap) throws CreateException {
        return new EnergyScheme(
                FeatureListFactory.from(new Size()),
                FeatureListFactory.from(
                        new MultiplyByConstant<>(new OverlapNumVoxels(), -1 * weightOverlap)),
                FeatureListFactory.empty(),
                RegionMapSingleton.instance(),
                new BoundingBoxIntersection());
    }
}
