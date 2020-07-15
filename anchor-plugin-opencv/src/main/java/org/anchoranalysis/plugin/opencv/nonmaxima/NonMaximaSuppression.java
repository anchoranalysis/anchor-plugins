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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;


/**
 * Non-maxima suppression, that keeps highest-confidence bounding boxes removing overlapping boxes. 
 * 
 * <ol>
 * <li>Select highest-confidence proposal, remove from list, add to output</li>
 * <li>Compare this proposal with remainder, and remove any remainder with a similarity score above a threshold</li>
 * <li>If there are remaining proposals in the queue, goto Step 1</li>
 * </ol>
 * 
 * @param T the type-of-object to which the algorithm applies
 * 
 * @author Owen Feehan
 *
 */
public abstract class NonMaximaSuppression<T> {

	/**
	 * Reduce a set of bounding-boxes (each with a confidence score) to a set with minimal overlap.
	 * 
	 * <p>See the class description for details of algorithm</p>
	 * 
	 * @param proposals proposed bounding-boxes with scores
	 * @param overlapThreshold after a proposal is accepted, other proposals with overlap greater-equal than this threshold are removed
	 * @return accepted proposals
	 */
	public List<WithConfidence<T>> apply(
		Collection<WithConfidence<T>> proposals,
		double overlapThreshold
	) {
		init( proposals );
		
		PriorityQueue<WithConfidence<T>> pq = new PriorityQueue<>(proposals);
		
		List<WithConfidence<T>> out = new ArrayList<>();
		
		while( !pq.isEmpty() ) {
			
			WithConfidence<T> highestConfidence = pq.poll();
			
			removeOverlapAboveScore(
				highestConfidence,
				pq,
				overlapThreshold
			);
			
			out.add(highestConfidence);
		}
		
		return out;
	}
	
	/** Called before the operation begins with all proposals */
	protected abstract void init( Collection<WithConfidence<T>> allProposals );
	
	/** A score calculating the overlap between two items */
	protected abstract double overlapScoreFor( T item1, T item2);

	/** Finds possible neighbours for a particular object efficiently */
	protected abstract Predicate<T> possibleOverlappingObjects( T src, Iterable<WithConfidence<T>> others );
	
	
	private void removeOverlapAboveScore( WithConfidence<T> proposal, PriorityQueue<WithConfidence<T>> others, double scoreThreshold ) {
		
		Predicate<T> pred = possibleOverlappingObjects(proposal.getObject(), others);
		
		Iterator<WithConfidence<T>> othersItr = others.iterator();
		while( othersItr.hasNext() ) {
			
			T other = othersItr.next().getObject();
			
			if (pred.test(other)) {
			
				double score = overlapScoreFor(proposal.getObject(), other);
				
				// These objects are deemed highly-similar and removed
				if (score >= scoreThreshold) {
					// Remove from the queue if above the threshold
					othersItr.remove();
				}
			}
		}
	}
}
