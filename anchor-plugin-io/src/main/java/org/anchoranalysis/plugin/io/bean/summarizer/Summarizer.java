/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.summarizer;

import java.util.Collection;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.stack.input.ImageMetadataInput;

/**
 * Summarizes a set of elements by outputting a descriptive string.
 *
 * @author Owen Feehan
 * @param <T> type of element to be summarized
 */
public abstract class Summarizer<T> extends AnchorBean<Summarizer<T>> {

    /**
     * Adds the elements to the summary.
     *
     * @param elements the elements to add.
     */
    public void addAll(Collection<T> elements) throws OperationFailedException {
        for (T element : elements) {
            add(element);
        }
    }

    /**
     * Adds a element to the summary.
     *
     * @param element the element to add.
     */
    public abstract void add(T element) throws OperationFailedException;

    /**
     * A string summarizing this item.
     *
     * @return the description.
     */
    public abstract String describe() throws OperationFailedException;

    /**
     * Whether a {@link ImageMetadataInput} is required as an input.
     *
     * @return true if the summarize requires {@link ImageMetadataInput} as input, or false if any
     *     input is acceptable.
     * @throws OperationFailedException if this cannot be established.
     */
    public abstract boolean requiresImageMetadata() throws OperationFailedException;
}
