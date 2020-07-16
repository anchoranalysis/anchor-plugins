/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.EigenvalueAndVector;

/**
 * The length of a principal-axis (as defined by Image Moments).
 *
 * @author Owen Feehan
 */
public class PrincipalAxisLength extends SpecificAxisBase {

    @Override
    protected double calcFeatureResultFromSpecificMoment(EigenvalueAndVector moment)
            throws FeatureCalcException {
        return moment.eigenvalueNormalizedAsAxisLength();
    }

    @Override
    protected double resultIfTooFewPixels() throws FeatureCalcException {
        throw new FeatureCalcException(
                "Too few voxels to determine axis-orientation and therefore axis-length");
    }
}
