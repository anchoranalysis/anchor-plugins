/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.points.calculate.ellipse.CalculateEllipseLeastSquares;
import org.anchoranalysis.plugin.points.calculate.ellipse.ObjectWithEllipse;

// Calculates the ellipticity of an object-mask (on the COG slice if it's a zstack)
public class Ellipticity extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        FeatureInputSingleObject inputSessionless = input.get();

        ObjectWithEllipse both;
        try {
            both = input.calc(new CalculateEllipseLeastSquares());
        } catch (FeatureCalcException e) {
            if (e.getCause() instanceof InsufficientPointsException) {
                // If we don't have enough points, we return perfectly ellipticity as it's so small
                return 1.0;
            } else {
                throw new FeatureCalcException(e);
            }
        }

        ObjectMask object = both.getObject();

        // If we have these few pixels, assume we are perfectly ellipsoid
        if (object.numPixelsLessThan(6)) {
            return 1.0;
        }

        return EllipticityCalculatorHelper.calc(
                object, both.getMark(), inputSessionless.getDimensionsRequired());
    }
}
