/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

public class Divide<T extends FeatureInput> extends FeatureListElem<T> {

    // START BEAN PROPERTIES
    @BeanField private boolean avoidDivideByZero = false;

    @BeanField private double divideByZeroValue = 1e+15;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        int size = getList().size();

        double result = input.calc(getList().get(0));

        for (int i = 1; i < size; i++) {
            double div = input.calc(getList().get(i));
            if (div == 0.0) {
                if (avoidDivideByZero) {
                    return divideByZeroValue;
                } else {
                    throw new FeatureCalcException(
                            String.format(
                                    "Divide by zero from feature %s",
                                    getList().get(i).getFriendlyName()));
                }
            }
            result /= div;
        }
        return result;
    }

    @Override
    public String getDscrLong() {
        return descriptionForList("/");
    }

    public boolean isAvoidDivideByZero() {
        return avoidDivideByZero;
    }

    public void setAvoidDivideByZero(boolean avoidDivideByZero) {
        this.avoidDivideByZero = avoidDivideByZero;
    }

    public double getDivideByZeroValue() {
        return divideByZeroValue;
    }

    public void setDivideByZeroValue(double divideByZeroValue) {
        this.divideByZeroValue = divideByZeroValue;
    }
}
