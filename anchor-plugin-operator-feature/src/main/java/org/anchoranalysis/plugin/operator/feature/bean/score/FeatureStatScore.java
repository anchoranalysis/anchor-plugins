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

package org.anchoranalysis.plugin.operator.feature.bean.score;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.score.FeatureResultSupplier;

/**
 * Calculates a score based upon the statistical mean and std-deviation
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public abstract class FeatureStatScore<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<T> itemMean;

    @BeanField @Getter @Setter private Feature<T> itemStdDev;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<T> input) throws FeatureCalculationException {

        return deriveScore(
                input.calc(getItem()), input.calc(itemMean), () -> input.calc(itemStdDev));
    }

    /**
     * Derive scores given the value, mean and standard-deviation
     *
     * @param featureValue the feature-value calculated from getItem()
     * @param mean the mean
     * @param stdDev a means to get the std-deviation (if needed)
     * @return
     * @throws FeatureCalculationException
     */
    protected abstract double deriveScore(
            double featureValue, double mean, FeatureResultSupplier stdDev)
            throws FeatureCalculationException;

    @Override
    public String describeParams() {
        return String.format(
                "%s,%s,%s",
                getItem().descriptionLong(),
                getItemMean().descriptionLong(),
                getItemStdDev().descriptionLong());
    }
}
