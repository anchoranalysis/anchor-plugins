/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;

public abstract class ChnlProviderOneMask extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private BinaryChnlProvider mask;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        Mask maskChnl = mask.create();
        DimChecker.check(chnl, maskChnl);
        return createFromMaskedChnl(chnl, maskChnl);
    }

    protected abstract Channel createFromMaskedChnl(Channel chnl, Mask mask) throws CreateException;

    public BinaryChnlProvider getMask() {
        return mask;
    }

    public void setMask(BinaryChnlProvider mask) {
        this.mask = mask;
    }
}
