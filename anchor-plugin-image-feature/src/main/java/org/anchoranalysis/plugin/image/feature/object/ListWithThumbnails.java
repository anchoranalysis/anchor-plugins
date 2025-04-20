/*-
 * #%L
 * anchor-plugin-image-feature
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
package org.anchoranalysis.plugin.image.feature.object;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;

/**
 * A list of items with an associated thumbnail-batch.
 *
 * @author Owen Feehan
 * @param <T> list-type
 * @param <S> thumbnail input type
 */
public class ListWithThumbnails<T, S> {

    /** The list of items. */
    private final List<T> list;

    /** The optional thumbnail batch associated with the list. */
    @Getter private final Optional<ThumbnailBatch<S>> thumbnailBatch;

    /**
     * Creates a new instance without a thumbnail batch.
     *
     * @param list the list of items
     */
    public ListWithThumbnails(List<T> list) {
        this.list = list;
        this.thumbnailBatch = Optional.empty();
    }

    /**
     * Creates a new instance with a thumbnail batch.
     *
     * @param list the list of items
     * @param thumbnailBatch the {@link ThumbnailBatch} associated with the list
     */
    public ListWithThumbnails(List<T> list, ThumbnailBatch<S> thumbnailBatch) {
        this.list = list;
        this.thumbnailBatch = Optional.of(thumbnailBatch);
    }

    /**
     * Gets the item at the specified index in the list.
     *
     * @param arg0 the index of the item to retrieve
     * @return the item at the specified index
     */
    public T get(int arg0) {
        return list.get(arg0);
    }

    /**
     * Gets the size of the list.
     *
     * @return the number of items in the list
     */
    public int size() {
        return list.size();
    }
}
