/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.opencv.nonmaxima;

import com.google.common.base.Predicate;
import java.util.Collection;
import java.util.Set;
import org.anchoranalysis.image.index.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.OverlapCalculator;
import org.anchoranalysis.image.object.combine.ObjectMaskMerger;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;

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
        return OverlapCalculator.calculateOverlapRatio(item1, item2, merged);
    }

    @Override
    protected Predicate<ObjectMask> possibleOverlappingObjects(
            ObjectMask src, Iterable<WithConfidence<ObjectMask>> others) {
        // All possible other objects as a hash-set
        Set<ObjectMask> possibleOthers = rTree.intersectsWith(src).stream().toSet();
        return possibleOthers::contains;
    }
}
