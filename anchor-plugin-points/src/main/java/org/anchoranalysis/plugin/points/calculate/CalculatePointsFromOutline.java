/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate;

import java.util.List;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.points.PointsFromObject;

@EqualsAndHashCode(callSuper = false)
public class CalculatePointsFromOutline
        extends FeatureCalculation<List<Point3i>, FeatureInputSingleObject> {

    @Override
    protected List<Point3i> execute(FeatureInputSingleObject params) throws FeatureCalcException {
        return PointsFromObject.pointsFromMaskOutline(params.getObject());
    }
}
