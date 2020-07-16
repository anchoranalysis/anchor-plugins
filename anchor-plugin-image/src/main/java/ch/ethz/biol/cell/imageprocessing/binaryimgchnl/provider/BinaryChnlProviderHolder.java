/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;

// We don't really use this as a bean, convenient way of inserting channels into providers in bean
// parameters
// This is hack
public class BinaryChnlProviderHolder extends BinaryChnlProvider {

    private Mask chnl;

    public BinaryChnlProviderHolder(Mask chnl) {
        super();
        this.chnl = chnl;
    }

    @Override
    public Mask create() throws CreateException {
        return chnl;
    }
}
