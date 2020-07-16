/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean.histogram;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelToWithHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.ChnlConverterAttached;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.histogram.ChnlConverterHistogramUpperLowerQuantileIntensity;

public class ChnlConverterHistogramBeanToByteUpperLowerQuantileIntensity
        extends ConvertChannelToWithHistogram {

    // START BEAN PROPERTIES
    @BeanField private double quantileLower = 0.0;

    @BeanField private double quantileUpper = 1.0;

    /** Sets the min by multiplying the quantileLower by this constant */
    @BeanField private double scaleLower = 1.0;

    /** Sets the max by multiplying the quantileUpper by this constant */
    @BeanField private double scaleUpper = 1.0;
    // END BEAN PROPERTIES

    @Override
    public ChnlConverterAttached<Histogram, ?> createConverter() {
        return new ChnlConverterHistogramUpperLowerQuantileIntensity(
                quantileLower, quantileUpper, scaleLower, scaleUpper);
    }

    public double getQuantileLower() {
        return quantileLower;
    }

    public void setQuantileLower(double quantileLower) {
        this.quantileLower = quantileLower;
    }

    public double getQuantileUpper() {
        return quantileUpper;
    }

    public void setQuantileUpper(double quantileUpper) {
        this.quantileUpper = quantileUpper;
    }

    public double getScaleLower() {
        return scaleLower;
    }

    public void setScaleLower(double scaleLower) {
        this.scaleLower = scaleLower;
    }

    public double getScaleUpper() {
        return scaleUpper;
    }

    public void setScaleUpper(double scaleUpper) {
        this.scaleUpper = scaleUpper;
    }
}
