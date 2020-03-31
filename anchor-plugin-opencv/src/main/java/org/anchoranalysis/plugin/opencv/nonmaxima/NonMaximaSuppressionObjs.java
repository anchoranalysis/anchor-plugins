package org.anchoranalysis.plugin.opencv.nonmaxima;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.anchoranalysis.image.index.rtree.ObjMaskCollectionRTree;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskOverlap;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;

import com.google.common.base.Predicate;

public class NonMaximaSuppressionObjs extends NonMaximaSuppression<ObjMask> {
	
	private ObjMaskCollectionRTree rTree;
	
	@Override
	protected void init(Collection<WithConfidence<ObjMask>> allProposals) {
		// NOTHING TO DO
		rTree = new ObjMaskCollectionRTree(
			removeWithConfidence(allProposals)
		);
	}
	
	@Override
	protected double overlapScoreFor(ObjMask item1, ObjMask item2) {
		ObjMask merged = ObjMaskMerger.merge(item1, item2);
		return ObjMaskOverlap.calcOverlapRatio(item1, item2, merged);
	}

	@Override
	protected Predicate<ObjMask> possibleOverlappingObjs(ObjMask src, Iterable<WithConfidence<ObjMask>> others) {
		// All possible other masks as a hash-set
		Set<ObjMask> possibleOthers = new HashSet<>( rTree.intersectsWith(src).asList() );
		return objToTest -> possibleOthers.contains(objToTest);
	}

	private static <T> List<T> removeWithConfidence( Collection<WithConfidence<T>> collection ) {
		return collection.stream().map(
			wc -> wc.getObj()
		).collect( Collectors.toList() );
	}
}
