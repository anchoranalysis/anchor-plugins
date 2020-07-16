/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Dimensions-calculation for one specific axis only.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public abstract class ForSpecificAxis<T extends FeatureInputNRG> extends FromDimensionsBase<T> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String axis = "x";
    // END BEAN PARAMETERS

    @Override
    protected double calcFromDims(ImageDimensions dim) {
        return calcForAxis(dim, AxisTypeConverter.createFromString(axis));
    }

    protected abstract double calcForAxis(ImageDimensions dimensions, AxisType axis);

    @Override
    public String getParamDscr() {
        return String.format("%s", axis);
    }
}
