/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;

// Repeats an input slice, to match dimensions
public class BinaryChnlProviderMaxIntensityProjection extends BinaryChnlProviderOne {

    // ASSUMES REGIONS ARE IDENTICAL
    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {
        return chnl.maxIntensityProj();
    }
}
