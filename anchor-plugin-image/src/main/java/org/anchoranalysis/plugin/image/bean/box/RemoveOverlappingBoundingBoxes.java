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

package org.anchoranalysis.plugin.image.bean.box;

import com.google.common.base.Predicate;
import java.util.List;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.NonMaximaSuppression;
import org.anchoranalysis.plugin.image.box.IntersectionOverUnion;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Non-maxima suppression for axis-aligned bounding-boxes using an <a
 * href="https://en.wikipedia.org/wiki/Jaccard_index">Intersection over Union</a> score.
 *
 * @see NonMaximaSuppression for a description of the algorithm.
 * @author Owen Feehan
 */
public class RemoveOverlappingBoundingBoxes extends NonMaximaSuppression<BoundingBox> {

    @Override
    protected void init(List<WithConfidence<BoundingBox>> allElements) {
        // NOTHING TO DO
    }

    /** As bounding box intersection test is cheap, we pass back all neighbors */
    @Override
    protected Predicate<BoundingBox> possibleOverlappingObjects(
            BoundingBox src, Iterable<WithConfidence<BoundingBox>> others) {
        // Accept all
        return irrelevant -> true;
    }

    /**
     * The Intersection over Union (IoU) score for two bounding-boxes
     *
     * @see <a
     *     href="https://www.quora.com/How-does-non-maximum-suppression-work-in-object-detection">Intersection-over-Union</a>
     * @param element1 the first bounding-box
     * @param element2 the second bounding-box
     * @return the IoU score
     */
    @Override
    protected double overlapScoreFor(BoundingBox element1, BoundingBox element2) {
        return IntersectionOverUnion.forBoxes(element1, element2);
    }
}
