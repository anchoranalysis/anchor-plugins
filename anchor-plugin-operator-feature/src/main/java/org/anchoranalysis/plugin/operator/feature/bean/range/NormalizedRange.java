/*-
 * #%L
 * anchor-plugin-operator-feature
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

package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.feature.bean.operator.FeatureBinary;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * The range of two feature values (i.e. max - min), divided by their mean.
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public class NormalizedRange<T extends FeatureInput> extends FeatureBinary<T> {

    // START BEAN PROPERTIES
    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<T> input) throws FeatureCalculationException {
        double val1 = input.calculate(getItem1());
        double val2 = input.calculate(getItem2());

        double absDiff = Math.abs(val1 - val2);
        double mean = (val1 + val2) / 2;

        if (mean == 0.0) {
            return 0.0;
        }

        return absDiff / mean;
    }

    @Override
    public String descriptionLong() {
        return String.format("%s - %s", getItem1().descriptionLong(), getItem2().descriptionLong());
    }
}
