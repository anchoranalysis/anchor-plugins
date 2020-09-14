package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.function.Consumer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.Getter;
import lombok.Setter;

/**
 * A strategy for reducing elements that greedily removes any element with a strong overlap with another.
 *
 * <p>The highest-confidence element is always retained as a priority over lower-confidence elements.
 * 
 * <p>The strength of the overlap is measured by a score with {@code 0 <= score <= 1}).
 * 
 * <p>See <a href="https://towardsdatascience.com/non-maximum-suppression-nms-93ce178e177c">Non-maximum suppression</a> for a description of the algorithm.
 * 
 * @author Owen Feehan
 * @param <T> the element-type that exists in the collection (with confidence)
 */
public abstract class NonMaximaSuppression<T> extends ReduceElements<T> {

    // START BEAN FIELDS
    /** Bounding boxes with scores above this threshold are removed */
    @BeanField @Getter @Setter private double scoreThreshold = 0.3;
    // END BEAN FIELDS
    
    @Override
    protected boolean includePossiblyOverlappingObject(T source, T other) {
        // These objects are deemed sufficiently-overlapping to be removed
        return overlapScoreFor(source, other) >= scoreThreshold;
    }
    
    @Override
    protected boolean processOverlapping(WithConfidence<T> source, WithConfidence<T> overlapping,
            Runnable removeOverlapping, Consumer<T> changeSource) {
        // This removes the overlapping object from the current iteration
        removeOverlapping.run();
        return true;
    }
        
    /** 
     * A score calculating the overlap between two elements.
     * 
     * @param element1 first-element
     * @param element2 second-element
     */
    protected abstract double overlapScoreFor(T element1, T element2);
}
