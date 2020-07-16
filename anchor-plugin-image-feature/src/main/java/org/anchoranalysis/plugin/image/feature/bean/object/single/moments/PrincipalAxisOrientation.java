/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.EigenvalueAndVector;

/**
 * An element from orientation of a principal-axis (as defined by Image Moments).
 *
 * @author Owen Feehan
 */
public class PrincipalAxisOrientation extends SpecificAxisBase {

    // START BEAN PROPERTIES
    /** Which axis to read from (x,y,z) */
    @BeanField private String axis = "x";
    // END BEAN PROPERTIES

    @Override
    protected double calcFeatureResultFromSpecificMoment(EigenvalueAndVector moment)
            throws FeatureCalcException {
        int axisIndex =
                AxisTypeConverter.dimensionIndexFor(AxisTypeConverter.createFromString(axis));
        return moment.getEigenvector().get(axisIndex);
    }

    @Override
    protected double resultIfTooFewPixels() throws FeatureCalcException {
        throw new FeatureCalcException("Too few voxels to determine axis-orientation");
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }
}
