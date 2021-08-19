/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.distance;

import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.mpp.feature.bean.energy.element.FeaturePairMemo;
import org.anchoranalysis.mpp.feature.input.FeatureInputPairMemo;
import org.anchoranalysis.spatial.point.Point3d;

/**
 * The minimum distance in any one particular axis-aligned direction (i.e. taking the distance as a
 * vector, the minimum element in the vector)
 *
 * @author Owen Feehan
 */
public class MinimumDistanceByDimension extends FeaturePairMemo {

    @Override
    public double calculate(SessionInput<FeatureInputPairMemo> input) {

        FeatureInputPairMemo params = input.get();

        Point3d centerPoint =
                distanceVector(
                        params.getObject1().getMark().centerPoint(),
                        params.getObject2().getMark().centerPoint());

        return minDimension(centerPoint, params.getObject1().getMark().numberDimensions() >= 3);
    }

    /** Calculates the distance between two points in each dimension independently */
    private static Point3d distanceVector(Point3d point1, Point3d point2) {
        Point3d point1Duplicated = new Point3d(point1);
        point1Duplicated.subtract(point2);
        point1Duplicated.absolute();
        return point1Duplicated;
    }

    private static double minDimension(Point3d cp, boolean hasZ) {
        double min = Math.min(cp.x(), cp.y());
        if (hasZ) {
            min = Math.min(min, cp.z());
        }
        return min;
    }
}
