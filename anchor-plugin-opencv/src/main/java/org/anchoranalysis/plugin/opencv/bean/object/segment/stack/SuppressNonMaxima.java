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
package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ReduceElements;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;

/**
 * Applies a segmentation procedure followed by non-maxima suppression.
 *
 * @author Owen Feehan
 * @param <T> model-type
 */
@NoArgsConstructor
@AllArgsConstructor
public class SuppressNonMaxima<T> extends SegmentStackIntoObjectsPooled<T> {

    // START BEAN PROPERTIES
    /** The segmentation algorithm that is applied as an input to non-maxima suppression. */
    @BeanField @Getter @Setter private SegmentStackIntoObjectsPooled<T> segment;

    /** The algorithm for reducing the number of object-masks. */
    @BeanField @Getter @Setter
    private ReduceElements<ObjectMask> reduce = new RemoveOverlappingObjects();
    // END BEAN PROPERTIES

    /**
     * Creates with a particular segmentation algorithm as an input.
     *
     * @param segment the segmentation algorithm to use, before applying non-maxima suppression.
     */
    public SuppressNonMaxima(SegmentStackIntoObjectsPooled<T> segment) {
        this.segment = segment;
    }

    @Override
    public ConcurrentModelPool<T> createModelPool(ConcurrencyPlan plan) {
        return segment.createModelPool(plan);
    }

    @Override
    public SegmentedObjects segment(Stack stack, ConcurrentModelPool<T> modelPool)
            throws SegmentationFailedException {
        SegmentedObjects objects = segment.segment(stack, modelPool);

        try {
            return new SegmentedObjects(reduce.reduce(objects.asList()));
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
