/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;

public class ChnlProviderMeanIntensityProjection extends ChnlProviderOne {

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        return chnl.meanIntensityProjection();
    }
}
