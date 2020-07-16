/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.HistogramProviderOne;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Removes all items in the histogram below a certain threshold.
 *
 * <p>The incoming histogram is replaced i.e. this operation is MUTABLE.
 *
 * @author Owen Feehan
 */
public class HistogramProviderAbove extends HistogramProviderOne {

    // START BEAN PROPERTIES
    @BeanField private int threshold = 0;
    // END BEAN PROPERTIES

    @Override
    protected Histogram createFromHistogram(Histogram hist) throws CreateException {
        hist.removeBelowThreshold(threshold);
        return hist;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
