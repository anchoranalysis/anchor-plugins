/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ChnlProviderGaussianBlurIJ extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Positive private double sigma = 3;
    // END BEAN PROPERTIES

    @SuppressWarnings("deprecation")
    private Channel blur(Channel chnl) {

        GaussianBlur gb = new GaussianBlur();

        VoxelBoxWrapper vb = chnl.getVoxelBox();

        // Are we missing a Z slice?
        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {
            ImageProcessor processor = IJWrap.imageProcessor(vb, z);
            gb.blur(processor, sigma); // NOSONAR
        }

        return chnl;
    }

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        return blur(chnl);
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }
}
