/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.annotation.io.assignment.Assignment;

public class AnnotationGroupList<T extends Assignment>
        implements Iterable<AnnotationGroup<T>>, IAddAnnotation<T> {

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
