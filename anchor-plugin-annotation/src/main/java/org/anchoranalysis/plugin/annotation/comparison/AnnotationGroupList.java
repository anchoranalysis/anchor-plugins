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

package org.anchoranalysis.plugin.annotation.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.annotation.io.assignment.Assignment;

public class AnnotationGroupList<T extends Assignment>
        implements Iterable<AnnotationGroup<T>>, AddAnnotation<T> {

    private List<AnnotationGroup<T>> delegate = new ArrayList<>();

    public AnnotationGroup<T> first() {
        return delegate.get(0);
    }

    public boolean add(AnnotationGroup<T> e) {
        return delegate.add(e);
    }

    public boolean addAll(Collection<AnnotationGroup<T>> e) {
        return delegate.addAll(e);
    }

    @Override
    public void addSkippedAnnotationImage() {
        for (AnnotationGroup<T> group : delegate) {
            group.addSkippedAnnotationImage();
        }
    }

    @Override
    public void addUnannotatedImage() {
        for (AnnotationGroup<T> group : delegate) {
            group.addUnannotatedImage();
        }
    }

    @Override
    public void addAcceptedAnnotation(T assignment) {
        for (AnnotationGroup<T> group : delegate) {
            group.addAcceptedAnnotation(assignment);
        }
    }

    @Override
    public Iterator<AnnotationGroup<T>> iterator() {
        return delegate.iterator();
    }
}
