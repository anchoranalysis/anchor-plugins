package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.box.BoundedList;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Combines object-masks by projecting the maximum confidence-level for each voxel and thresholding.
 * 
 * <p>After thresholding, a connected-components algorithm splits the thresholded-mask into single-objects.
 * 
 * <p>This is a more efficient approach for merging adjacent segments than {@link ConditionallyMergeOverlappingObjects},
 * especially if there are very many overlapping objects occupying the same space.
 * 
 * <p>However, unlike {@link ConditionallyMergeOverlappingObjects}, it does not distinguish
 * easily between regions of different levels of confidence, beyond a simple threshold, which are then merged
 * together if spatially adjacent.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor
public class ThresholdConfidence extends ReduceElements<ObjectMask> {
    
    // START BEAN PROPERTIES
    /** The minimum confidence of an element for its object-mask to be included. */ 
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    
    /** The minimum number of voxels that must exist in a connected-component to be included. */
    @BeanField @Getter @Setter private int minNumberVoxels = 5;
    // END BEAN PROPERTIES
    
    /**
     * Creates with a minimum-confidence level.
     *  
     * @param minConfidence the minimum confidence of an element for its object-mask to be included.
     */
    public ThresholdConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    @Override
    public List<WithConfidence<ObjectMask>> reduce(List<WithConfidence<ObjectMask>> elements) throws OperationFailedException {

        if (elements.isEmpty()) {
            // An empty input list produces an empty output list
            return new ArrayList<>();
        }
        
        BoundedList<WithConfidence<ObjectMask>> boundedList = new BoundedList<>(elements, withConfidence -> withConfidence.getElement().boundingBox() ); 
        return DeriveObjectsFromList.deriveObjects(boundedList, elements, minConfidence, minNumberVoxels);
    }
}
