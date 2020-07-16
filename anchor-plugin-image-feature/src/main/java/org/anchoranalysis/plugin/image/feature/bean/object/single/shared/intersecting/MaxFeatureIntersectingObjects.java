/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import java.util.Comparator;
import java.util.List;

/**
 * 1. Finds all objects from an object-collection whose bounding-boxes intersect with a particular
 * object. 2. Calculates a pairwise-feature 3. Returns the maximum
 *
 * @author Owen Feehan
 */
public class MaxFeatureIntersectingObjects extends FeatureIntersectingObjectsSingleElement {

    @Override
    protected double aggregateResults(List<Double> results) {
        return results.stream()
                .max(Comparator.comparing(Double::valueOf))
                .orElse(getValueNoObjects());
    }
}
