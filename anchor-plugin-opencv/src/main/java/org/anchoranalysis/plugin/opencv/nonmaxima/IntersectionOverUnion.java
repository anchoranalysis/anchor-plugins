package org.anchoranalysis.plugin.opencv.nonmaxima;

import java.util.Optional;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.OverlapCalculator;
import org.anchoranalysis.image.object.combine.ObjectMaskMerger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Calculates the intersection-over-union scores for different object pairs.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class IntersectionOverUnion {

    public static double forObjects(ObjectMask object1, ObjectMask object2) {
        ObjectMask merged = ObjectMaskMerger.merge(object1, object2);
        return OverlapCalculator.calculateOverlapRatio(object1, object2, merged);
    }
    
    public static double forBoxes(BoundingBox box1, BoundingBox box2) {
        Optional<BoundingBox> intersection = box1.intersection().with(box2);
        if (!intersection.isPresent()) {
            // If there's no intersection then the score is 0
            return 0.0;
        }

        long intersectionArea = intersection.get().extent().calculateVolume();

        // The total area is equal to the sum of both minus the intersection (which is otherwise
        // counted twice)
        long total =
                box2.extent().calculateVolume()
                        + box1.extent().calculateVolume()
                        - intersectionArea;

        return ((double) intersectionArea) / total;
    }
}
