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

package org.anchoranalysis.plugin.operator.feature.bean.statistics;

import cern.jet.random.Normal;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.statistics.FeatureResultSupplier;

/**
 * Calculates a score between 0 and 1 based on the Cumulative Distribution Function (CDF) of a
 * Gaussian distribution. As the value approaches the mean, the score approaches 1.0.
 *
 * @param <T> the type of {@link FeatureInput} this feature operates on
 */
public class GaussianCumulative<T extends FeatureInput> extends StatisticalBase<T> {

    /** If true, always returns 1.0 for values higher than the mean. */
    @BeanField @Getter @Setter
    private boolean ignoreHigherSide = false; // Always returns 1.0 for the higher side

    /** If true, always returns 1.0 for values lower than the mean. */
    @BeanField @Getter @Setter
    private boolean ignoreLowerSide = false; // Always returns 1.0 for the lower side

    /** If true, treats the higher side as if it's the full CDF. */
    @BeanField @Getter @Setter private boolean rewardHigherSide = false;

    /** If true, treats the lower side as if it's 1 minus the full CDF. */
    @BeanField @Getter @Setter private boolean rewardLowerSide = false;

    @Override
    protected double deriveScore(double featureValue, double mean, FeatureResultSupplier stdDev)
            throws FeatureCalculationException {

        if (ignoreHigherSide && featureValue > mean) {
            return 1.0;
        }

        if (ignoreLowerSide && featureValue < mean) {
            return 1.0;
        }

        return calc(mean, stdDev.get(), featureValue, rewardHigherSide, rewardLowerSide);
    }

    /**
     * Calculates the score based on the Gaussian CDF.
     *
     * @param mean the mean of the Gaussian distribution
     * @param stdDev the standard deviation of the Gaussian distribution
     * @param val the value to calculate the score for
     * @param rewardHigherSide if true, reward higher values
     * @param rewardLowerSide if true, reward lower values
     * @return the calculated score
     */
    private static double calc(
            double mean,
            double stdDev,
            double val,
            boolean rewardHigherSide,
            boolean rewardLowerSide) {
        Normal normal = new Normal(mean, stdDev, null);
        double cdf = normal.cdf(val);

        if (rewardHigherSide) {
            return cdf;
        }

        if (rewardLowerSide) {
            return (1 - cdf);
        }

        if (val > mean) {
            return (1 - cdf) * 2;
        } else {
            return cdf * 2;
        }
    }
}
