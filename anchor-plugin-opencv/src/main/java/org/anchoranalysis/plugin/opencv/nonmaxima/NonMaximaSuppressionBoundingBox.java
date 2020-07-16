/* (C)2020 */
package org.anchoranalysis.plugin.opencv.nonmaxima;

import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Optional;
import org.anchoranalysis.image.extent.BoundingBox;

/**
 * Non-maxima suppression for axis-aligned bounding-boxes using an Intersection over Union score.
 *
 * @author Owen Feehan
 */
public class NonMaximaSuppressionBoundingBox extends NonMaximaSuppression<BoundingBox> {

    @Override
    protected void init(Collection<WithConfidence<BoundingBox>> allProposals) {
        // NOTHING TO DO
    }

    /**
     * The Intersection over Union (IoU) score for two bounding-boxes.
     *
     * @see <a
     *     href="https://www.quora.com/How-does-non-maximum-suppression-work-in-object-detection">Intersection-over-Union</a>
     * @param second the other bounding-box to consider when calculating the IoU
     * @return the IoU score
     */
    @Override
    protected double overlapScoreFor(BoundingBox obj1, BoundingBox obj2) {

        Optional<BoundingBox> intersection = obj1.intersection().with(obj2);
        if (!intersection.isPresent()) {
            // If there's no intersection then the score is 0
            return 0.0;
        }

        long intersectionArea = intersection.get().extent().getVolume();

        // The total area is equal to the sum of both minus the intersection (which is otherwise
        // counted twice)
        long total = obj2.extent().getVolume() + obj1.extent().getVolume() - intersectionArea;

        return ((double) intersectionArea) / total;
    }

    /** As bounding box intersection test is cheap, we pass back all neighbors */
    @Override
    protected Predicate<BoundingBox> possibleOverlappingObjects(
            BoundingBox src, Iterable<WithConfidence<BoundingBox>> others) {
        // Accept all
        return a -> true;
    }
}
