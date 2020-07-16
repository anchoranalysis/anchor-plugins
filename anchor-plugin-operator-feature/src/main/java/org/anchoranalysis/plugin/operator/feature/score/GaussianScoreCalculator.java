/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.score;

import cern.jet.random.Normal;

/**
 * Calculates a Gaussian Score
 *
 * @author Owen Feehan
 */
public class GaussianScoreCalculator {

    private GaussianScoreCalculator() {}

    public static double calc(
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
