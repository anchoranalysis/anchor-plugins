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
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.score.FeatureResultSupplier;
import org.anchoranalysis.plugin.operator.feature.score.GaussianScoreCalculator;

// A score between 0 and 1, based upon the CDF of a guassian. as one approaches the mean, the score
// approaches 1.0
public class GaussianScore<T extends FeatureInput> extends FeatureStatScore<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private boolean ignoreHigherSide = false; // Always returns 1.0 for the higher side

    @BeanField @Getter @Setter
    private boolean ignoreLowerSide = false; // Always returns 1.0 for the lower side

    /** Treat the higher side as if it's the the fill cdf */
    @BeanField @Getter @Setter private boolean rewardHigherSide = false;

    /* Treat the lower side as if it's 1 - the fill cdf */
    @BeanField @Getter @Setter private boolean rewardLowerSide = false;
    // END BEAN PROPERTIES

    @Override
    protected double deriveScore(
            double featureValue,
            double mean,
            FeatureResultSupplier stdDev)
            throws FeatureCalculationException {

        if (ignoreHigherSide && featureValue > mean) {
            return 1.0;
        }

        if (ignoreLowerSide && featureValue < mean) {
            return 1.0;
        }

        return GaussianScoreCalculator.calc(
                mean, stdDev.get(), featureValue, rewardHigherSide, rewardLowerSide);
    }
}
