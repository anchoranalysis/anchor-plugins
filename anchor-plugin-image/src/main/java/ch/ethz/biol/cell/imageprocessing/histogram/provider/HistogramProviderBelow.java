/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.HistogramProviderOne;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * This cuts a Histogram below a threshold BUT TRANSFERS ALL THE COUNT above the threshold into the
 * just below the threshold value
 *
 * <p>Note that this is NOT SYMMETRIC behaviour with HistogramProviderAbove
 *
 * @author Owen Feehan
 */
public class HistogramProviderBelow extends HistogramProviderOne {

    // START BEAN PROPERTIES
    @BeanField private int threshold = 0;

    @BeanField private boolean merge = false;
    // END BEAN PROPERTIES

    @Override
    public Histogram createFromHistogram(Histogram h) throws CreateException {

        for (int i = threshold; i <= h.getMaxBin(); i++) {
            h.transferVal(i, threshold - 1);
        }
        return h;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }
}
