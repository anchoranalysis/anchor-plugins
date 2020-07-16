/* (C)2020 */
package org.anchoranalysis.plugin.annotation.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.core.text.TypedValue;

@RequiredArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnnotationGroup<T extends Assignment> implements IAddAnnotation<T> {

    // START REQUIRED ARGUMENTS
    @Getter @EqualsAndHashCode.Include private final String identifier;
    // END REQUIRED ARGUMENTS

    @Getter private int numImagesWithAcceptedAnnotations = 0;

    @Getter private int numImagesWithSkippedAnnotations = 0;

    @Getter private int numImagesWithoutAnnotations = 0;

    @Override
    public void addSkippedAnnotationImage() {
        numImagesWithSkippedAnnotations++;
    }

    @Override
    public void addUnannotatedImage() {
        numImagesWithoutAnnotations++;
    }

    @Override
    public void addAcceptedAnnotation(T assignment) {
        numImagesWithAcceptedAnnotations++;
    }

    public List<String> createHeaders() {
        return new ArrayList<>(
                Arrays.asList(
                        "name",
                        "percentAnnotated",
                        "percentSkipped",
                        "cntImages",
                        "cntImagesAnnotations",
                        "cntImagesAcceptedAnnotations",
                        "cntImagesSkippedAnnotations",
                        "cntImagesWithoutAnnotations"));
    }

    public List<TypedValue> createValues() {
        return new ArrayList<>(
                Arrays.asList(
                        new TypedValue(identifier, false),
                        new TypedValue(percentAnnotatedImages(), 2),
                        new TypedValue(percentSkippedAnnotations(), 2),
                        new TypedValue(numImagesTotal(), 2),
                        new TypedValue(numImagesAnnotations(), 2),
                        new TypedValue(numImagesWithAcceptedAnnotations(), 2),
                        new TypedValue(numImagesWithSkippedAnnotations(), 2),
                        new TypedValue(numImagesWithoutAnnotations(), 2)));
    }

    public int numImagesTotal() {
        return numImagesWithAcceptedAnnotations
                + numImagesWithoutAnnotations
                + numImagesWithSkippedAnnotations;
    }

    public int numImagesAnnotations() {
        return numImagesWithAcceptedAnnotations + numImagesWithSkippedAnnotations;
    }

    public double percentAnnotatedImages() {
        return ((double) numImagesAnnotations() * 100) / numImagesTotal();
    }

    public double percentSkippedAnnotations() {
        return ((double) (numImagesWithSkippedAnnotations)) * 100 / numImagesAnnotations();
    }
}
