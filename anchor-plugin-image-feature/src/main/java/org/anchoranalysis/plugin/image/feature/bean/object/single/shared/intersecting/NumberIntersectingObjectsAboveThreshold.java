/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import java.util.List;

/**
 * 1. Finds all objects from an object-collection whose bounding-boxes intersect with a particular
 * object. 2. Calculates a pairwise-feature 3. Returns the maximum
 *
 * @author Owen Feehan
 */
public class NumberIntersectingObjectsAboveThreshold extends FeatureIntersectingObjectsThreshold {

    @Override
    protected double aggregateResults(List<Double> results) {

        int cnt = 0;

        // We loop through each intersecting bounding box, and take the one with the highest
        // feature-value
        for (double val : results) {

            if (val >= getThreshold()) {
                cnt++;
            }
        }

        return cnt;
    }
}
