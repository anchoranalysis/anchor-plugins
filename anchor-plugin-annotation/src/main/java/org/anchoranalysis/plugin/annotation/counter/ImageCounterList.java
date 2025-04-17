/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.counter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Allows operations to be applied to several {@link ImageCounter}s collectively.
 *
 * @author Owen Feehan
 * @param <T> the payload-type of each {@link ImageCounter}.
 */
public class ImageCounterList<T extends Assignment<ObjectMask>>
        implements Iterable<ImageCounter<T>>, ImageCounter<T> {

    private List<ImageCounter<T>> delegate = new ArrayList<>();

    /**
     * Adds an {@link ImageCounter} to the list.
     *
     * @param e the {@link ImageCounter} to add.
     * @return true if the counter was added successfully, false otherwise.
     */
    public boolean add(ImageCounter<T> e) {
        return delegate.add(e);
    }

    /**
     * Adds a collection of {@link ImageCounter}s to the list.
     *
     * @param <S> the type of {@link ImageCounter}s in the collection.
     * @param e the collection of {@link ImageCounter}s to add.
     * @return true if the collection was added successfully, false otherwise.
     */
    public <S extends ImageCounter<T>> boolean addAll(Collection<S> e) {
        return delegate.addAll(e);
    }

    @Override
    public void addUnannotatedImage() {
        for (ImageCounter<T> group : delegate) {
            group.addUnannotatedImage();
        }
    }

    @Override
    public void addAnnotatedImage(T payload) {
        for (ImageCounter<T> group : delegate) {
            group.addAnnotatedImage(payload);
        }
    }

    @Override
    public Iterator<ImageCounter<T>> iterator() {
        return delegate.iterator();
    }

    /**
     * Returns a sequential {@link Stream} with this list as its source.
     *
     * @return a sequential {@link Stream} over the elements in this list.
     */
    public Stream<ImageCounter<T>> stream() {
        return delegate.stream();
    }
}