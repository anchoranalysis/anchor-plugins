/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.CalculateEllipsoidLeastSquares;

public abstract class EllipsoidBase extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** Iff true, supresses covariance in the z-direction. */
    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        ObjectMask object = input.get().getObject();

        // If we have these few pixels, assume we are perfectly ellipsoid
        if (object.numPixelsLessThan(12)) {
            return 1.0;
        }

        MarkEllipsoid me = CalculateEllipsoidLeastSquares.of(input, suppressZ);

        return calc(input.get(), me);
    }

    protected abstract double calc(FeatureInputSingleObject input, MarkEllipsoid me)
            throws FeatureCalcException;
}
