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

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MeanIntensityDifference extends FeatureSingleMemo {

    // START BEAN PROPERTIES
    @BeanField private double minDiff;
    // END BEAN PROPERTIES

    public MeanIntensityDifference() {
        super();
    }

    public MeanIntensityDifference(double minDiff) {
        super();
        this.minDiff = minDiff;
    }

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> params) throws FeatureCalcException {

        VoxelizedMark pm = params.get().getPxlPartMemo().voxelized();

        double mean_in =
                pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_INSIDE).mean();
        double mean_shell =
                pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_SHELL).mean();

        return ((mean_in - mean_shell) - minDiff) / 255;
    }

    @Override
    public String getParamDscr() {
        return String.format("minDiff=%f", minDiff);
    }

    public double getMinDiff() {
        return minDiff;
    }

    public void setMinDiff(double minDiff) {
        this.minDiff = minDiff;
    }
}
