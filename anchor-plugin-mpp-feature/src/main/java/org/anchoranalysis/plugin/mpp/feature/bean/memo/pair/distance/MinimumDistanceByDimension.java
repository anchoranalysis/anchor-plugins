/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.distance;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.SessionInput;

/**
 * The minimum distance in any one particular axis-aligned direction (i.e. taking the distance as a
 * vector, the minimum element in the vector)
 *
 * @author Owen Feehan
 */
public class MinimumDistanceByDimension extends FeaturePairMemo {

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) {

        FeatureInputPairMemo params = input.get();

        Point3d cp =
                distanceVector(
                        params.getObj1().getMark().centerPoint(),
                        params.getObj2().getMark().centerPoint());

        return minDimension(cp, params.getObj1().getMark().numDims() >= 3);
    }

    /** Calculates the distance between two points in each dimension independently */
    private static Point3d distanceVector(Point3d point1, Point3d point2) {
        Point3d cp = new Point3d(point1);
        cp.subtract(point2);
        cp.absolute();
        return cp;
    }

    private static double minDimension(Point3d cp, boolean hasZ) {
        double min = Math.min(cp.getX(), cp.getY());
        if (hasZ) {
            min = Math.min(min, cp.getZ());
        }
        return min;
    }
}
