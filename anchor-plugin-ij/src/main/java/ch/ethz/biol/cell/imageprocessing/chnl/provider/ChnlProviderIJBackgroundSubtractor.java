/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.ImagePlus;
import ij.plugin.filter.BackgroundSubtracter;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;

public class ChnlProviderIJBackgroundSubtractor extends ChnlProviderFilterRadiusBase {

    @Override
    protected Channel createFromChnl(Channel chnl, int radius) throws CreateException {
        return subtractBackground(chnl, radius, true);
    }

    public static Channel subtractBackground(Channel chnl, int radius, boolean doPreSmooth) {
        ImagePlus imp = IJWrap.createImagePlus(chnl);

        BackgroundSubtracter plugin = new BackgroundSubtracter();
        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {
            plugin.rollingBallBackground(
                    imp.getStack().getProcessor(z + 1),
                    radius,
                    false,
                    false,
                    false,
                    doPreSmooth,
                    true);
        }

        return IJWrap.chnlFromImagePlus(imp, chnl.getDimensions().getRes());
    }
}
