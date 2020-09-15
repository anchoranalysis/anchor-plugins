/*-
 * #%L
 * anchor-plugin-image
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
