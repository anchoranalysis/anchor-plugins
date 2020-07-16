/* (C)2020 */
package org.anchoranalysis.plugin.opencv.nonmaxima;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Wraps an object with a confidence-score and imposes an ordering (highest confidence first)
 *
 * @param T object to be associated with a confidence score
 */
@AllArgsConstructor
@EqualsAndHashCode
public class WithConfidence<T> implements Comparable<WithConfidence<T>> {

    @Getter private T object;

    @Getter private double confidence;

    @Override
    public int compareTo(WithConfidence<T> other) {
        return Double.compare(other.confidence, confidence);
    }
}
