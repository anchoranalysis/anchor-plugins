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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.OverlapCalculator;
import org.anchoranalysis.image.objectmask.ops.ObjMaskMerger;

import com.google.common.base.Predicate;

public class NonMaximaSuppressionObjs extends NonMaximaSuppression<ObjectMask> {
	
	private ObjMaskCollectionRTree rTree;
	
	@Override
	protected void init(Collection<WithConfidence<ObjectMask>> allProposals) {
		// NOTHING TO DO
		rTree = new ObjMaskCollectionRTree(
			removeWithConfidence(allProposals)
		);
	}
	
	@Override
	protected double overlapScoreFor(ObjectMask item1, ObjectMask item2) {
		ObjectMask merged = ObjMaskMerger.merge(item1, item2);
		return OverlapCalculator.calcOverlapRatio(item1, item2, merged);
	}

	@Override
	protected Predicate<ObjectMask> possibleOverlappingObjs(ObjectMask src, Iterable<WithConfidence<ObjectMask>> others) {
		// All possible other masks as a hash-set
		Set<ObjectMask> possibleOthers = new HashSet<>( rTree.intersectsWith(src).asList() );
		return objToTest -> possibleOthers.contains(objToTest);
	}

	private static <T> List<T> removeWithConfidence( Collection<WithConfidence<T>> collection ) {
		return collection.stream().map(
			wc -> wc.getObj()
		).collect( Collectors.toList() );
	}
}
