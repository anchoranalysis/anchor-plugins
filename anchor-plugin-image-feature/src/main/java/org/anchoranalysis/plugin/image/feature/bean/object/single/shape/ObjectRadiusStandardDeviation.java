/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.core.points.PointsFromObject;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Calculates the standard deviation of distances from surface voxels to the centroid of an {@link
 * ObjectMask}.
 *
 * <p>Optionally, it can calculate the coefficient of variation instead of the standard deviation.
 */
public class ObjectRadiusStandardDeviation extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /**
     * If true, returns the coefficient of variation (stdDev/mean) instead of standard deviation.
     */
    @BeanField @Getter @Setter private boolean cov = false;
    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        ObjectMask object = input.get().getObject();

        // Get the outline
        List<Point3i> pointsOutline = PointsFromObject.listFromOutline3i(object);

        // Distances from the center to each point on the outline
        DoubleArrayList distances = distancesToPoints(object.centerOfGravity(), pointsOutline);

        return calculateStatistic(distances);
    }

    /**
     * Calculates distances from a point to a list of points.
     *
     * @param pointFrom the reference point to calculate distances from
     * @param pointsTo the list of points to calculate distances to
     * @return a {@link DoubleArrayList} containing the calculated distances
     */
    private static DoubleArrayList distancesToPoints(Point3d pointFrom, List<Point3i> pointsTo) {
        DoubleArrayList distances = new DoubleArrayList(pointsTo.size());
        for (Point3i point : pointsTo) {

            Point3d shifted = new Point3d(point.x() + 0.5, point.y() + 0.5, point.z() + 0.5);

            distances.add(pointFrom.distance(shifted));
        }
        return distances;
    }

    /**
     * Calculates either the standard deviation or coefficient of variation of distances.
     *
     * @param distances the list of distances to analyze
     * @return the calculated statistic (standard deviation or coefficient of variation)
     */
    private double calculateStatistic(DoubleArrayList distances) {
        // Calculate Std Deviation
        int size = distances.size();
        double sum = Descriptive.sum(distances);

        double sumOfSquares = Descriptive.sumOfSquares(distances);
        double variance = Descriptive.variance(size, sum, sumOfSquares);
        double stdDev = Descriptive.standardDeviation(variance);

        if (cov) {
            double mean = sum / size;
            return stdDev / mean;
        } else {
            return stdDev;
        }
    }
}
