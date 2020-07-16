/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;

public class BinaryChnlProviderAlternativeIfEmpty extends BinaryChnlProviderElseBase {

    @Override
    protected boolean condition(Mask chnl) throws CreateException {
        return chnl.hasHighValues();
    }
}
