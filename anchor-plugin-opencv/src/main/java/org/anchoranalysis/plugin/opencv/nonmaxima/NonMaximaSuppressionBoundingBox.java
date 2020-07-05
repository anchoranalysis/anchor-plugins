package org.anchoranalysis.plugin.opencv.nonmaxima;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.Collection;
import java.util.Optional;

import org.anchoranalysis.image.extent.BoundingBox;

import com.google.common.base.Predicate;

/**
 * Non-maxima suppression for axis-aligned bounding-boxes using an Intersection over Union score.
 * 
 * @author Owen Feehan
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

		Optional<BoundingBox> intersection = obj1.intersection().with(obj2);
		if (!intersection.isPresent()) {
			// If there's no intersection then the score is 0
			return 0.0;
		}
		
		long intersectionArea = intersection.get().extent().getVolume();
		
		// The total area is equal to the sum of both minus the intersection (which is otherwise counted twice)
		long total = obj2.extent().getVolume() + obj1.extent().getVolume() - intersectionArea;
		
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
