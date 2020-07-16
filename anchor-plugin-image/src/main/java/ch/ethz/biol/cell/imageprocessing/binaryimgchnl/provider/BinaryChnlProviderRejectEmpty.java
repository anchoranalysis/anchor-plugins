/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;

public class BinaryChnlProviderRejectEmpty extends BinaryChnlProviderOne {

    @Override
    public Mask createFromChnl(Mask binaryImgChnl) throws CreateException {
        if (!binaryImgChnl.hasHighValues()) {
            throw new CreateException("binaryImgChnl has no high values");
        }
        return binaryImgChnl;
    }
}
