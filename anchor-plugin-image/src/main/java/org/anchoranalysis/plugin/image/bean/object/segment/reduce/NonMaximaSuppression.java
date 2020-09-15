/*-
 * #%L
 * anchor-plugin-image
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

import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * A strategy for reducing elements that greedily removes any element with a strong overlap with
 * another.
 *
 * <p>The highest-confidence element is always retained as a priority over lower-confidence
 * elements.
 *
 * <p>The strength of the overlap is measured by a score with {@code 0 <= score <= 1}).
 *
 * <p>See <a
 * href="https://towardsdatascience.com/non-maximum-suppression-nms-93ce178e177c">Non-maximum
 * suppression</a> for a description of the algorithm.
 *
 * @author Owen Feehan
 * @param <T> the element-type that exists in the collection (with confidence)
 */
public abstract class NonMaximaSuppression<T> extends ReduceElementsGreedy<T> {

    // START BEAN FIELDS
    /** Bounding boxes with scores above this threshold are removed */
    @BeanField @Getter @Setter private double scoreThreshold = 0.3;
    // END BEAN FIELDS

    @Override
    protected boolean includePossiblyOverlappingObject(T source, T other) {
        // These objects are deemed sufficiently-overlapping to be removed
        return overlapScoreFor(source, other) >= scoreThreshold;
    }

    @Override
    protected boolean processOverlapping(
            WithConfidence<T> source,
            WithConfidence<T> overlapping,
            Runnable removeOverlapping,
            Consumer<T> changeSource) {
        // This removes the overlapping object from the current iteration
        removeOverlapping.run();
        return true;
    }

    /**
     * A score calculating the overlap between two elements.
     *
     * @param element1 first-element
     * @param element2 second-element
     */
    protected abstract double overlapScoreFor(T element1, T element2);
}
