package org.anchoranalysis.plugin.opencv.bean.text;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Non-maxima suppression, that keeps highest-confidence bounding boxes removing overlapping boxes. 
 * 
 * <ol>
 * <li>Select highest-confidence proposal, remove from list, add to output</li>
 * <li>Compare this proposal with remainder, and remove any remainder with overlap above a threshold</li>
 * <li>If there are remaining proposals in the queue, goto Step 1</li>
 * </ol>
 * 
 * @author owen
 *
 */
class NonMaximaSuppression {

	/**
	 * Reduce a set of bounding-boxes (each with a confidence score) to a set with minimal overlap.
	 * 
	 * <p>See the class description for details of algorithm</p>
	 * 
	 * @param proposals proposed bounding-boxes with scores
	 * @param overlapThreshold after a proposal is accepted, other proposals with overlap greater-equal than this threshold are removed
	 * @return accepted proposals
	 */
	public static List<BoundingBoxWithConfidence> apply( Collection<BoundingBoxWithConfidence> proposals, double overlapThreshold ) {
		
		PriorityQueue<BoundingBoxWithConfidence> pq = new PriorityQueue<>(proposals);
		
		List<BoundingBoxWithConfidence> out = new ArrayList<>();
		
		while( !pq.isEmpty() ) {
			
			BoundingBoxWithConfidence highestConfidence = pq.poll();
			
			removeOverlapping( highestConfidence, pq, overlapThreshold);
			
			out.add(highestConfidence);
		}
		
		return out;
	}
	
	private static void removeOverlapping( BoundingBoxWithConfidence proposal, PriorityQueue<BoundingBoxWithConfidence> others, double overlapThreshold ) {
		
		Iterator<BoundingBoxWithConfidence> itr = others.iterator();
		while( itr.hasNext() ) {
			
			BoundingBoxWithConfidence bbox = itr.next();
			
			double overlap = proposal.intersectionOverUnion(bbox.getBBox());
			
			if (overlap >= overlapThreshold) {
				// Remove from the queue if above the threshold
				itr.remove();
			}
		}
	}
}
