/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.plugin.image.bean.blur.BlurGaussian3D;
import org.anchoranalysis.plugin.image.bean.blur.BlurStrategy;

/**
 * Blurs an image
 *
 * <p>This is a mutable operation that alters the current image
 *
 * @author Owen Feehan
 */
public class ChnlProviderBlur extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private BlurStrategy strategy = new BlurGaussian3D();
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        try {
            strategy.blur(chnl.getVoxelBox(), chnl.getDimensions(), getLogger().messageLogger());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }

        return chnl;
    }

    public BlurStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(BlurStrategy strategy) {
        this.strategy = strategy;
    }
}
