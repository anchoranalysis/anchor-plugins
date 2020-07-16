/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;

public class ChnlProviderIfHistogramExists extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private HistogramProvider histogramProvider;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Histogram hist = histogramProvider.create();

        if (hist != null) {
            return chnl;
        } else {
            throw new CreateException("histogram is null");
        }
    }

    public HistogramProvider getHistogramProvider() {
        return histogramProvider;
    }

    public void setHistogramProvider(HistogramProvider histogramProvider) {
        this.histogramProvider = histogramProvider;
    }
}
