/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class Ellipsoidicity extends EllipsoidBase {

    @Override
    protected double calc(FeatureInputSingleObject input, MarkEllipsoid me)
            throws FeatureCalcException {

        return EllipticityCalculatorHelper.calc(
                input.getObject(), me, input.getDimensionsRequired());
    }
}
