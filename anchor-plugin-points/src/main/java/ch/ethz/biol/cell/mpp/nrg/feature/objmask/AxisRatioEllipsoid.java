/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class AxisRatioEllipsoid extends EllipsoidBase {

    @Override
    protected double calc(FeatureInputSingleObject input, MarkEllipsoid me)
            throws FeatureCalcException {

        double[] radii = me.radiiOrdered();

        return radii[0] / radii[1];
    }
}
