/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.scale.ScaleFactor;

public class BinaryChnlProviderScaleXY extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private boolean interpolate = true;
    // END BEAN PROPERTIES

    public static Mask scale(Mask chnl, ScaleCalculator scaleCalculator, Interpolator interpolator)
            throws CreateException {

        ScaleFactor scaleFactor;
        try {
            scaleFactor = scaleCalculator.calc(chnl.getDimensions());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }

        if (scaleFactor.isNoScale()) {
            // Nothing to do
            return chnl;
        }

        return chnl.scaleXY(scaleFactor, interpolator);
    }

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {
        Interpolator interpolator =
                interpolate
                        ? InterpolatorFactory.getInstance().binaryResizing()
                        : InterpolatorFactory.getInstance().noInterpolation();
        return scale(chnl, scaleCalculator, interpolator);
    }
}
