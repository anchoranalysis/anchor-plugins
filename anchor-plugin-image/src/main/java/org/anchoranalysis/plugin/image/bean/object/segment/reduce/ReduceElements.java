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

import java.util.List;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Reduces the number or spatial-extent of elements by favouring higher-confidence elements over lower-confidence elements.
 *
 * @param <T> the element-type that exists in the collection (with confidence)
 * @author Owen Feehan
 */
public abstract class ReduceElements<T> extends AnchorBean<ReduceElements<T>> {
    
    /**
     * Reduce a list of elements (each with a confidence score) to a smaller-list.
     *
     * <p>See the class javadoc for details of algorithm.
     * 
     * <p>It is not guaranteed that the resulting list will have fewer elements than the input list, but never more.
     *
     * @param elements proposed bounding-boxes with scores
     * @return accepted proposals
     * @throws OperationFailedException if anything goes wrong
     */
    public abstract List<WithConfidence<T>> reduce(List<WithConfidence<T>> elements) throws OperationFailedException;
}
