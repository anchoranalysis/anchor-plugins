/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.logical.BinaryChnlAnd;
import org.anchoranalysis.image.binary.mask.Mask;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderAnd extends BinaryChnlProviderReceive {

    // ASSUMES REGIONS ARE IDENTICAL
    @Override
    protected Mask createFromChnlReceive(Mask chnl, Mask receiveChnl) throws CreateException {
        BinaryChnlAnd.apply(chnl, receiveChnl);
        return chnl;
    }
}
