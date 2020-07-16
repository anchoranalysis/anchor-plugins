/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;

public class ChnlProviderRejectIfNoSignal extends ChnlProviderOne {

    // START BEANS
    @BeanField private int minIntensity = 40;

    @BeanField private double minRatio = 0.01;
    // END BEANS

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Histogram h = HistogramFactory.create(chnl);

        double percent = h.percentGreaterEqualTo(minIntensity);

        if (percent < minRatio) {
            throw new CreateException(
                    String.format(
                            "Rejecting as %f < %f for intensity %d",
                            percent, minRatio, minIntensity));
        }
        return chnl;
    }

    public int getMinIntensity() {
        return minIntensity;
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity = minIntensity;
    }

    public double getMinRatio() {
        return minRatio;
    }

    public void setMinRatio(double minRatio) {
        this.minRatio = minRatio;
    }
}
