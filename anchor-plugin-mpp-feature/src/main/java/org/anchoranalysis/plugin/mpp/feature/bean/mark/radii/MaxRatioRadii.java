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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;

public class MaxRatioRadii extends FeatureMark {

    @Override
    public double calculate(SessionInput<FeatureInputMark> input) throws FeatureCalculationException {

        if (!(input.get().getMark() instanceof MarkEllipse)) {
            throw new FeatureCalculationException(
                    "Mark must be of type " + MarkEllipse.class.getName());
        }

        MarkEllipse mark = (MarkEllipse) input.get().getMark();

        double radius1 = mark.getRadii().x();
        double radius2 = mark.getRadii().y();

        assert (!Double.isNaN(radius1));
        assert (!Double.isNaN(radius2));

        if (radius1 == 0 || radius2 == 0) {
            return 0.0;
        }

        return Math.max(radius1 / radius2, radius2 / radius1);
    }
}
