/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ChnlProviderScale extends ChnlProviderOne {

    // Start BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter
    private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
    // End BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        return scale(chnl, scaleCalculator, interpolator.create(), getLogger().messageLogger());
    }

    public static Channel scale(
            Channel chnl,
            ScaleCalculator scaleCalculator,
            Interpolator interpolator,
            MessageLogger logger)
            throws CreateException {
        try {
            logger.logFormatted("incoming Image Resolution: %s\n", chnl.getDimensions().getRes());

            ScaleFactor scaleFactor = scaleCalculator.calc(chnl.getDimensions());

            logger.logFormatted("Scale Factor: %s\n", scaleFactor.toString());

            Channel chnlOut = chnl.scaleXY(scaleFactor, interpolator);

            logger.logFormatted(
                    "outgoing Image Resolution: %s\n", chnlOut.getDimensions().getRes());

            return chnlOut;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
