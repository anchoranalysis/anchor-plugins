/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.HistogramProviderOne;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.intensity.HistogramThresholder;

/**
 * Thresholds a histogram using a CalculateLevel keeping only the values greater equal than the
 * threshold
 *
 * @author Owen Feehan
 */
public class HistogramProviderAboveCalculateLevel extends HistogramProviderOne {

    // START BEAN PROPERTIES
    @BeanField private CalculateLevel calculateLevel;
    // END BEAN PROPERTIES

    @Override
    public Histogram createFromHistogram(Histogram hist) throws CreateException {
        try {
            return HistogramThresholder.withCalculateLevel(hist, calculateLevel);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    public CalculateLevel getCalculateLevel() {
        return calculateLevel;
    }

    public void setCalculateLevel(CalculateLevel calculateLevel) {
        this.calculateLevel = calculateLevel;
    }
}
