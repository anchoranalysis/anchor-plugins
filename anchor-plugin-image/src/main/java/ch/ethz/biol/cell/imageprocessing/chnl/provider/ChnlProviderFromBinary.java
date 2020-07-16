/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;

public class ChnlProviderFromBinary extends ChnlProviderMask {

    @Override
    protected Channel createFromMask(Mask mask) throws CreateException {
        return mask.getChannel();
    }
}
