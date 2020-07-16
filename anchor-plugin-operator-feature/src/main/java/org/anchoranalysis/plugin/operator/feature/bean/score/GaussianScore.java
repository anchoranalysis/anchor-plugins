/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.score;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.score.GaussianScoreCalculator;

// A score between 0 and 1, based upon the CDF of a guassian. as one approaches the mean, the score
// approaches 1.0
public class GaussianScore<T extends FeatureInput> extends FeatureStatScore<T> {

    // START BEAN PROPERTIES
    @BeanField private boolean ignoreHigherSide = false; // Always returns 1.0 for the higher side

    @BeanField private boolean ignoreLowerSide = false; // Always returns 1.0 for the lower side

    @BeanField
    private boolean rewardHigherSide = false; // Treat the higher side as if it's the the fill cdf

    @BeanField
    private boolean rewardLowerSide = false; // Treat the lower side as if it's 1 - the fill cdf
    // END BEAN PROPERTIES

    @Override
    protected double deriveScore(
            double featureValue, double mean, Operation<Double, FeatureCalcException> stdDev)
            throws FeatureCalcException {

        if (ignoreHigherSide && featureValue > mean) {
            return 1.0;
        }

        if (ignoreLowerSide && featureValue < mean) {
            return 1.0;
        }

        return GaussianScoreCalculator.calc(
                mean, stdDev.doOperation(), featureValue, rewardHigherSide, rewardLowerSide);
    }

    public boolean isIgnoreHigherSide() {
        return ignoreHigherSide;
    }

    public void setIgnoreHigherSide(boolean ignoreHigherSide) {
        this.ignoreHigherSide = ignoreHigherSide;
    }

    public boolean isIgnoreLowerSide() {
        return ignoreLowerSide;
    }

    public void setIgnoreLowerSide(boolean ignoreLowerSide) {
        this.ignoreLowerSide = ignoreLowerSide;
    }

    public boolean isRewardHigherSide() {
        return rewardHigherSide;
    }

    public void setRewardHigherSide(boolean rewardHigherSide) {
        this.rewardHigherSide = rewardHigherSide;
    }

    public boolean isRewardLowerSide() {
        return rewardLowerSide;
    }

    public void setRewardLowerSide(boolean rewardLowerSide) {
        this.rewardLowerSide = rewardLowerSide;
    }
}
