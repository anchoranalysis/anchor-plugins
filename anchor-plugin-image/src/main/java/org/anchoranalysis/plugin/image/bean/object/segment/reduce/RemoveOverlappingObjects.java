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

package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Set;
import org.anchoranalysis.image.extent.rtree.ObjectCollectionRTree;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.OverlapCalculator;
import org.anchoranalysis.image.object.combine.ObjectMaskMerger;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Non-maxima suppression for object-masks using an <a
 * href="https://en.wikipedia.org/wiki/Jaccard_index">Intersection over Union</a> score.
 *
 * @see NonMaximaSuppression for a description of the algorithm.
 * @author Owen Feehan
 */
public class RemoveOverlappingObjects extends NonMaximaSuppression<ObjectMask> {

    private ObjectCollectionRTree rTree;

    @Override
    protected void init(List<WithConfidence<ObjectMask>> allElements) {
        // NOTHING TO DO
        rTree =
                new ObjectCollectionRTree(
                        ObjectCollectionFactory.mapFrom(allElements, WithConfidence::getElement));
    }

    @Override
    protected Predicate<ObjectMask> possibleOverlappingObjects(
            ObjectMask source, Iterable<WithConfidence<ObjectMask>> others) {
        // All possible other objects as a hash-set
        Set<ObjectMask> possibleOthers = rTree.intersectsWith(source).stream().toSet();
        return possibleOthers::contains;
    }

    @Override
    protected double overlapScoreFor(ObjectMask element1, ObjectMask element2) {
        ObjectMask merged = ObjectMaskMerger.merge(element1, element2);
        return OverlapCalculator.calculateOverlapRatio(element1, element2, merged);
    }
}
