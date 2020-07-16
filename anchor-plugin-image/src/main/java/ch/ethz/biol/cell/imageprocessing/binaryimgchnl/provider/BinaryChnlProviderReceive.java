/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;

public abstract class BinaryChnlProviderReceive extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private BinaryChnlProvider receive;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {

        Mask receiveChnl = receive.create();
        return createFromChnlReceive(chnl, receiveChnl);
    }

    protected abstract Mask createFromChnlReceive(Mask chnl, Mask receiveChnl)
            throws CreateException;

    public BinaryChnlProvider getReceive() {
        return receive;
    }

    public void setReceive(BinaryChnlProvider receive) {
        this.receive = receive;
    }
}
