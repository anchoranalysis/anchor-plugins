/* (C)2020 */
package org.anchoranalysis.plugin.opencv.nonmaxima;

import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Set;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.OverlapCalculator;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

public class NonMaximaSuppressionObjects extends NonMaximaSuppression<ObjectMask> {

    private ObjectCollectionRTree rTree;

    @Override
    protected void init(Collection<WithConfidence<ObjectMask>> allProposals) {
        // NOTHING TO DO
        rTree =
                new ObjectCollectionRTree(
                        ObjectCollectionFactory.mapFrom(allProposals, WithConfidence::getObject));
    }

    @Override
    protected double overlapScoreFor(ObjectMask item1, ObjectMask item2) {
        ObjectMask merged = ObjectMaskMerger.merge(item1, item2);
        return OverlapCalculator.calcOverlapRatio(item1, item2, merged);
    }

    @Override
    protected Predicate<ObjectMask> possibleOverlappingObjects(
            ObjectMask src, Iterable<WithConfidence<ObjectMask>> others) {
        // All possible other masks as a hash-set
        Set<ObjectMask> possibleOthers = rTree.intersectsWith(src).stream().toSet();
        return possibleOthers::contains;
    }
}
