/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.plugin.filter.RankFilters;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;

public class ChnlProviderVarianceFilterIJ2D extends ChnlProviderFilterRadiusBase {

    @Override
    public Channel createFromChnl(Channel chnl, int radius) throws CreateException {
        return RankFilterUtilities.applyEachSlice(chnl, radius, RankFilters.VARIANCE);
    }
}
