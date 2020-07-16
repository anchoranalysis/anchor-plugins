/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;

public abstract class BinaryChnlProviderElseBase extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinaryChnlProvider binaryChnlElse;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromChnl(Mask chnl) throws CreateException {
        if (condition(chnl)) {
            return chnl;
        } else {
            return binaryChnlElse.create();
        }
    }

    /**
     * If this evaluates true, the binary-channel will be returned as-is, otherwise {@link
     * binaryChnlElse} is returned
     */
    protected abstract boolean condition(Mask chnl) throws CreateException;
}
