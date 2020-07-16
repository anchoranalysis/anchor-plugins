/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;

public class HistogramProviderChnl extends HistogramProvider {

    // START BEAN PROPERTIES
    @BeanField private ChnlProvider chnl;

    @BeanField @OptionalBean private BinaryChnlProvider mask;
    // END BEAN PROPERTIES

    @Override
    public Histogram create() throws CreateException {

        Channel chnlIn = chnl.create();

        if (mask != null) {
            return HistogramFactory.create(chnlIn, mask.create());
        } else {
            return HistogramFactory.create(chnlIn);
        }
    }

    public ChnlProvider getChnl() {
        return chnl;
    }

    public void setChnl(ChnlProvider chnl) {
        this.chnl = chnl;
    }

    public BinaryChnlProvider getMask() {
        return mask;
    }

    public void setMask(BinaryChnlProvider mask) {
        this.mask = mask;
    }
}
