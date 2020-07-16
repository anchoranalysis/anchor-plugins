/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

import org.anchoranalysis.annotation.io.assignment.Assignment;

public interface IAddAnnotation<T extends Assignment> {

    void addSkippedAnnotationImage();

    void addUnannotatedImage();

    void addAcceptedAnnotation(T assignment);
}
