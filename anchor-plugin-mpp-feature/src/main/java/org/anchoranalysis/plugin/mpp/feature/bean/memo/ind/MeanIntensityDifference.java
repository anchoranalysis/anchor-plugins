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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.mpp.feature.bean.energy.element.FeatureSingleMemo;
import org.anchoranalysis.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.voxelized.VoxelizedMark;

@NoArgsConstructor
@AllArgsConstructor
public class MeanIntensityDifference extends FeatureSingleMemo {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double minDiff;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputSingleMemo> params)
            throws FeatureCalculationException {

        VoxelizedMark mark = params.get().getPxlPartMemo().voxelized();

        double meanInside =
                mark.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_INSIDE).mean();
        double meanShell =
                mark.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_SHELL).mean();

        return ((meanInside - meanShell) - minDiff) / 255;
    }

    @Override
    public String describeParams() {
        return String.format("minDiff=%f", minDiff);
    }
}
