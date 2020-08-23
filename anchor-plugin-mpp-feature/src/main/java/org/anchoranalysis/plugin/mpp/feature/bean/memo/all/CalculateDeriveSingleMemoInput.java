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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.all;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculate.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateDeriveSingleMemoInput
        extends FeatureCalculation<FeatureInputSingleMemo, FeatureInputAllMemo> {

    private final int index;

    @Override
    protected FeatureInputSingleMemo execute(FeatureInputAllMemo input) {
        FeatureInputSingleMemo paramsInd =
                new FeatureInputSingleMemo(null, input.getEnergyStackOptional());
        paramsInd.setPxlPartMemo(input.getPxlPartMemo().getMemoForIndex(index));
        return paramsInd;
    }
}