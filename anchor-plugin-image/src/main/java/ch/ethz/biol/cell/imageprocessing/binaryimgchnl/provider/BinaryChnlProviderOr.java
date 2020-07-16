/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.logical.BinaryChnlOr;
import org.anchoranalysis.image.binary.mask.Mask;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderOr extends BinaryChnlProviderReceive {

    // ASSUMES REGIONS ARE IDENTICAL
    @Override
    protected Mask createFromChnlReceive(Mask chnl, Mask receiveChnl) throws CreateException {
        BinaryChnlOr.binaryOr(chnl, receiveChnl);

        return chnl;
    }
}
