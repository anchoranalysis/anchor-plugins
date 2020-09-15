package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ReduceElements;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.RemoveOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentStackIntoObjectsPooled;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Applies a segmentation procedure followed by non-maxima suppression.
 * 
 * @author Owen Feehan
 * @param <T> model-type
 *
 */
@NoArgsConstructor @AllArgsConstructor
public class SuppressNonMaxima<T> extends SegmentStackIntoObjectsPooled<T> {

    // START BEAN PROPERTIES
    /** The segmentation algorithm that is applied as an input to non-maxima suppression. */
    @BeanField @Getter @Setter private SegmentStackIntoObjectsPooled<T> segment;
    
    /** The algorithm for reducing the number of object-masks. */ 
    @BeanField @Getter @Setter private ReduceElements<ObjectMask> reduce = new RemoveOverlappingObjects(); 
    // END BEAN PROPERTIES

    /**
     * Creates with a particular segmentation algorithm as an input.
     * 
     * @param segment the segmentation algorithm to use, before applying non-maxima suppression.
     */
    public SuppressNonMaxima(SegmentStackIntoObjectsPooled<T> segment) {
        this.segment = segment;
    }
    
    @Override
    public ConcurrentModelPool<T> createModelPool(ConcurrencyPlan plan) {
        return segment.createModelPool(plan);
    }

    @Override
    public SegmentedObjects segment(Stack stack, ConcurrentModelPool<T> modelPool)
            throws SegmentationFailedException {
        SegmentedObjects objects = segment.segment(stack, modelPool);
        
        try {
            return new SegmentedObjects( reduce.reduce(objects.asList()) );
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
