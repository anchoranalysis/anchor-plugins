package org.anchoranalysis.plugin.opencv.nonmaxima;

import java.util.Collection;
import org.anchoranalysis.image.extent.BoundingBox;

import com.google.common.base.Predicate;

/**
 * Non-maxima suppression for axis-aligned bounding-boxes using an Intersection over Union score.
 * 
 * @author owen
 *
 */
public class NonMaximaSuppressionBoundingBox extends NonMaximaSuppression<BoundingBox> {

	@Override
	protected void init(Collection<WithConfidence<BoundingBox>> allProposals) {
		// NOTHING TO DO
	}
	
	/**
	 * The Intersection over Union (IoU) score for two bounding-boxes.
	 * 
	 * @see <a href="https://www.quora.com/How-does-non-maximum-suppression-work-in-object-detection">Intersection-over-Union</a>
	 * @param second the other bounding-box to consider when calculating the IoU
	 * @return the IoU score
	 */
	@Override
	protected double overlapScoreFor(BoundingBox obj1, BoundingBox obj2) {

		BoundingBox intersection = new BoundingBox(obj1);
		if (!intersection.intersect(obj2, true)) {
			// If there's no intersection then the score is 0
			return 0.0;
		}
		
		int intersectionArea = intersection.extnt().getVolume();
		
		// The total area is equal to the sum of both minus the intersection (which is otherwise counted twice)
		int total = obj2.extnt().getVolume() + obj1.extnt().getVolume() - intersectionArea;
		
		return ((double) intersectionArea) / total;
	}

	/** As bounding box intersection test is cheap, we pass back all neighbours */
	@Override
	protected Predicate<BoundingBox> possibleOverlappingObjs(BoundingBox src,
			Iterable<WithConfidence<BoundingBox>> others) {
		// Accept all
		return a->true;
	}
}
