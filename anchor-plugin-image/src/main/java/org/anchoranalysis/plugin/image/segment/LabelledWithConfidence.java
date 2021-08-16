package org.anchoranalysis.plugin.image.segment;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Like {@link WithConfidence} but additionally adds a label.
 *
 * @param <T> element to be associated with a confidence score
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode
public class LabelledWithConfidence<T> implements Comparable<LabelledWithConfidence<T>> {

    /** The label associated with the element. */
    @Getter private final String label;

    /** The element with associated confidence. */
    @Getter private final WithConfidence<T> withConfidence;

    public LabelledWithConfidence(T element, double confidence, String label) {
        this.withConfidence = new WithConfidence<>(element, confidence);
        this.label = label;
    }

    @Override
    public int compareTo(LabelledWithConfidence<T> other) {
        int confidenceComparison = withConfidence.compareTo(other.withConfidence);
        if (confidenceComparison == 0) {
            return label.compareTo(other.label);
        } else {
            return confidenceComparison;
        }
    }

    public T getElement() {
        return withConfidence.getElement();
    }

    public double getConfidence() {
        return withConfidence.getConfidence();
    }

    public void setElement(T element) {
        withConfidence.setElement(element);
    }

    @Override
    public String toString() {
        return String.format("label=%s,%s", label, withConfidence);
    }
}
