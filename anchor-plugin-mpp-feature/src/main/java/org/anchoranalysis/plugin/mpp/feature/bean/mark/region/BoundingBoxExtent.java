/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.region;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.plugin.mpp.feature.bean.unit.UnitConverter;

public class BoundingBoxExtent extends FeatureMarkRegion {

    // START BEAN PARAMETERS
    @BeanField private String axis = "x";

    @BeanField private UnitConverter unit = new UnitConverter();
    // END BEAN PARAMETERS

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

        ImageDimensions dimensions = input.get().getDimensionsRequired();

        BoundingBox bbox = input.get().getMark().bbox(dimensions, getRegionID());

        return resolveDistance(
                bbox, Optional.of(dimensions.getRes()), AxisTypeConverter.createFromString(axis));
    }

    private double resolveDistance(
            BoundingBox bbox, Optional<ImageResolution> res, AxisType axisType)
            throws FeatureCalcException {
        return unit.resolveDistance(
                bbox.extent().getValueByDimension(axisType),
                res,
                unitVector(AxisTypeConverter.dimensionIndexFor(axisType)));
    }

    private DirectionVector unitVector(int dimIndex) {
        DirectionVector dirVector = new DirectionVector();
        dirVector.setIndex(dimIndex, 1);
        return dirVector;
    }

    @Override
    public String getParamDscr() {
        return String.format("%s", axis);
    }

    public String getAxis() {
        return axis;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public UnitConverter getUnit() {
        return unit;
    }

    public void setUnit(UnitConverter unit) {
        this.unit = unit;
    }
}
