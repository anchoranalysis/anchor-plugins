/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.score;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Calculates a score based upon the statistical mean and std-deviation
 *
 * @author Owen Feehan
 * @param <T> feature input-type
 */
public abstract class FeatureStatScore<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

    // START BEAN PROPERTIES
    @BeanField private Feature<T> itemMean = null;

    @BeanField private Feature<T> itemStdDev = null;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

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
     * @throws FeatureCalcException
     */
    protected abstract double deriveScore(
            double featureValue, double mean, Operation<Double, FeatureCalcException> stdDev)
            throws FeatureCalcException;

    public Feature<T> getItemMean() {
        return itemMean;
    }

    public void setItemMean(Feature<T> itemMean) {
        this.itemMean = itemMean;
    }

    public Feature<T> getItemStdDev() {
        return itemStdDev;
    }

    public void setItemStdDev(Feature<T> itemStdDev) {
        this.itemStdDev = itemStdDev;
    }

    @Override
    public String getParamDscr() {
        return String.format(
                "%s,%s,%s",
                getItem().getDscrLong(),
                getItemMean().getDscrLong(),
                getItemStdDev().getDscrLong());
    }
}
