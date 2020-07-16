/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.util.List;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculates the mean of the intensity-gradient defined by multiple NRG channels in a particular
 * direction
 *
 * <p>An NRG channel is present for X, Y and optionally Z intensity-gradients.
 *
 * <p>A constant is subtracted from the NRG channel (all positive) to center around 0
 *
 * @author Owen Feehan
 */
public class GradientMagnitude extends IntensityGradientBase {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        // Calculate the mean
        double sum = 0.0;

        List<Point3d> points = input.calc(gradientCalculation());

        for (Point3d point : points) {
            // Calculate the norm of the point
            sum += point.l2norm();
        }

        return sum / points.size();
    }
}
