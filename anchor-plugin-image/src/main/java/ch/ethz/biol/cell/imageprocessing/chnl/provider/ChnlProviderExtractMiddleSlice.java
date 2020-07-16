/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;

public class ChnlProviderExtractMiddleSlice extends ChnlProviderOne {

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        int z = (chnl.getDimensions().getZ() - 1) / 2;
        return chnl.extractSlice(z);
    }
}
