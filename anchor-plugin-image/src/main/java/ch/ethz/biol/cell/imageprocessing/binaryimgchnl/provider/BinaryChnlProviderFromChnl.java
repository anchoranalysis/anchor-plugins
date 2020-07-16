/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;

public class BinaryChnlProviderFromChnl extends BinaryChnlProviderChnlSource {

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {
        return new Mask(chnlSource, BinaryValues.getDefault());
    }
}
