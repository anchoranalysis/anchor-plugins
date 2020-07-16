/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.ImagePlus;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;

public class ChnlProviderMedianFilterIJ3D extends ChnlProviderOne {

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        ImagePlus imp = IJWrap.createImagePlus(chnl);

        Hybrid_3D_Median_Filter plugin = new Hybrid_3D_Median_Filter();
        plugin.setup("", imp);

        ImagePlus impOut = plugin.Hybrid3dMedianizer(imp);

        return IJWrap.chnlFromImagePlus(impOut, chnl.getDimensions().getRes());
    }
}
