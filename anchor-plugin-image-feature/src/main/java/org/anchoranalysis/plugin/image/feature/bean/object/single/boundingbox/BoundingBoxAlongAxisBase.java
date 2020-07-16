/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.boundingbox;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public abstract class BoundingBoxAlongAxisBase extends FeatureSingleObject {

    // START BEAN PARAMETERS
    @BeanField private String axis = "x";
    // END BEAN PARAMETERS

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        FeatureInputSingleObject inputSessionless = input.get();

        ReadableTuple3i point =
                extractTupleForBoundingBox(inputSessionless.getObject().getBoundingBox());

        return calcAxisValue(point);
    }

    protected abstract ReadableTuple3i extractTupleForBoundingBox(BoundingBox bbox);

    private double calcAxisValue(ReadableTuple3i point) {
        return point.getValueByDimension(AxisTypeConverter.createFromString(axis));
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
}
