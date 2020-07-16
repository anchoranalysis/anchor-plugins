/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculates deterministicly a point that is definitely inside the object mask. A selected axis is
 * outputted.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class ArbitraryInsidePoint extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String axis = "x";

    @BeanField @Getter @Setter private double emptyValue = 0;
    // END BEAN PROPERTIES

    public ArbitraryInsidePoint(String axis) {
        this.axis = axis;
    }

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        AxisType axisType = AxisTypeConverter.createFromString(axis);

        Optional<Point3i> arbPoint = input.get().getObject().findArbitraryOnVoxel();
        return arbPoint.map(point -> (double) point.getValueByDimension(axisType))
                .orElse(emptyValue);
    }
}
