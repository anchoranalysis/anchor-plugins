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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Reduces the number or spatial-extent of elements by favouring higher-confidence elements over
 * lower-confidence elements.
 *
 * <p>Elements can be removed entirely, or can have other operations executed (e.g. removing
 * overlapping voxels).
 *
 * <p>
 *
 * <ol>
 *   <li>Select highest-confidence proposal, remove from list, add to output.
 *   <li>Compare this proposal with remaining proposals, and process any overlapping elements
 *       (removing or otherwise altering).
 *   <li>If there are remaining proposals in the queue, goto Step 1.
 * </ol>
 *
 * @param <T> the element-type that exists in the collection (with confidence)
 * @author Owen Feehan
 */
public abstract class ReduceElementsGreedy<T> extends ReduceElements<T> {

    @Override
    public List<WithConfidence<T>> reduce(List<WithConfidence<T>> elements) {
        init(elements);

        PriorityQueue<WithConfidence<T>> queue = new PriorityQueue<>(elements);

        List<WithConfidence<T>> out = new ArrayList<>();

        while (!queue.isEmpty()) {

            WithConfidence<T> highestConfidence = queue.peek();

            if (!findOverlappingAndProcess(highestConfidence, queue)) {
                out.add(queue.poll());
            }
        }

        return out;
    }

    /** Called before the operation begins with all proposals */
    protected abstract void init(List<WithConfidence<T>> allElements);

    /**
     * Finds a subset of {@code others} that possibly overlap with the {@code source} element.
     *
     * <p>This is not a definite list of element, but must contain all possible overlapping element,
     * and should be efficient.
     *
     * <p>This is the <b>first</b> filtering step. The resulting list will then be further filtered
     * by {@link #includePossiblyOverlappingObject}.
     *
     * @param source the element we are currently processing, for which we search for possibly
     *     overlapping element
     * @param others other element that could possibly overlap with {@code source} but need to be
     *     filtered.
     */
    protected abstract Predicate<T> possibleOverlappingObjects(
            T source, Iterable<WithConfidence<T>> others);

    /**
     * Whether to include another (possibly-overlapping with {@code source}) element in processing?
     *
     * <p>This is the <b>second</b> filtering step (after {@link #possibleOverlappingObjects(Object,
     * Iterable)}.
     *
     * @param source the element we are currently processing, for which we search for possibly
     *     overlapping element
     * @param other another object which we are considering to include in processing (in conjunction
     *     with {@code source}).
     * @return true iff {@code other} should be included in processing.
     */
    protected abstract boolean includePossiblyOverlappingObject(T source, T other);

    /**
     * Processes an object that is deemed to overlap
     *
     * @param source the element that is being processed as the <i>source</i>, the object with
     *     higher confidence.
     * @param overlapping the element that is deemed to overlap with the {@code sourceElement} about
     *     which a decision is to be made.
     * @param removeOverlapping a runnable that can be called to remove {@code overlappingElement},
     *     if this decision is taken.
     * @param changeSource a runnable that can ne called to change the source-element, if this
     *     decision is taken.
     * @return true if the {@code source} object was altered during processing, false if it was not.
     */
    protected abstract boolean processOverlapping(
            WithConfidence<T> source,
            WithConfidence<T> overlapping,
            Runnable removeOverlapping,
            Consumer<T> changeSource);

    private boolean findOverlappingAndProcess(
            WithConfidence<T> proposal, PriorityQueue<WithConfidence<T>> others) {

        Predicate<T> predicate = possibleOverlappingObjects(proposal.getElement(), others);

        Iterator<WithConfidence<T>> othersIterator = others.iterator();
        while (othersIterator.hasNext()) {

            WithConfidence<T> other = othersIterator.next();

            if (includeElement(predicate, proposal, other)
                    && processOverlapping(
                            proposal, other, othersIterator::remove, proposal::setElement)) {
                return true;
            }
        }

        return false;
    }

    private boolean includeElement(
            Predicate<T> predicate, WithConfidence<T> proposal, WithConfidence<T> other) {
        return predicate.test(other.getElement())
                && includePossiblyOverlappingObject(proposal.getElement(), other.getElement());
    }
}
