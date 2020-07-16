/* (C)2020 */
package ch.ethz.biol.cell.mpp.anneal;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;

public class AnnealSchemeGeom extends AnnealScheme {

    // START BEAN PROPERTIES
    @BeanField @Positive private double rate = -1;

    @BeanField @Positive private double currentTemp = -1;
    // END BEAN PROPERTIES

    public AnnealSchemeGeom() {
        super();
    }

    public AnnealSchemeGeom(double initTemp, double rate) {
        super();
        this.rate = rate;
        this.currentTemp = initTemp;
    }

    // Guaranteed to be called only once for each iteration
    @Override
    public final double calcTemp(int iter) {

        this.currentTemp *= this.rate;
        return this.currentTemp;
    }

    @Override
    public final double crntTemp() {
        return this.currentTemp;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = currentTemp;
    }
}
