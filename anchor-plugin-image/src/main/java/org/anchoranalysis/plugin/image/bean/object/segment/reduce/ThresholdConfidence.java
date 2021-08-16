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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.spatial.box.BoundedList;
import org.anchoranalysis.spatial.rtree.SpatiallySeparate;

/**
 * Combines object-masks by projecting the maximum confidence-level for each voxel and thresholding.
 *
 * <p>After thresholding, a connected-components algorithm splits the thresholded-mask into
 * single-objects.
 *
 * <p>This is a more efficient approach for merging adjacent segments than {@link
 * ConditionallyMergeOverlappingObjects}, especially if there are very many overlapping objects
 * occupying the same space.
 *
 * <p>However, unlike {@link ConditionallyMergeOverlappingObjects}, it does not distinguish easily
 * between regions of different levels of confidence, beyond a simple threshold, which are then
 * merged together if spatially adjacent.
 *
 * @author Owen Feehan
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
    public List<LabelledWithConfidence<ObjectMask>> reduceLabelled(
            List<LabelledWithConfidence<ObjectMask>> elements) throws OperationFailedException {

        if (elements.isEmpty()) {
            // An empty input list produces an empty output list
            return new ArrayList<>();
        }

        // Take the label from the first element
        String label = elements.get(0).getLabel();

        // Check that all other labels are the same, otherwise we cannot proceed
        if (anyLabelDiffersTo(elements, label)) {
            throw new OperationFailedException(
                    "Labels are not all identical, so this reduction operation cannot proceed.");
        }

        SpatiallySeparate<LabelledWithConfidence<ObjectMask>> separate =
                new SpatiallySeparate<>(
                        withConfidence -> withConfidence.getElement().boundingBox());

        List<LabelledWithConfidence<ObjectMask>> out = new ArrayList<>();

        // For efficiency on rasters sparsely populated with objects, process each
        //  spatially-connected set of objects separately.
        for (Set<LabelledWithConfidence<ObjectMask>> split : separate.separate(elements)) {
            out.addAll(deriveLabelledObjects(Lists.newArrayList(split), label));
        }

        return out;
    }

    private List<LabelledWithConfidence<ObjectMask>> deriveLabelledObjects(
            List<LabelledWithConfidence<ObjectMask>> elements, String label)
            throws OperationFailedException {

        List<WithConfidence<ObjectMask>> elementsWithOutLabel =
                FunctionalList.mapToList(elements, LabelledWithConfidence::getWithConfidence);

        List<WithConfidence<ObjectMask>> derived = deriveObjects(elementsWithOutLabel);

        return FunctionalList.mapToList(
                derived, object -> new LabelledWithConfidence<>(label, object));
    }

    /** Checks if any label is different to {@code labelToCOmpare}. */
    private static <T> boolean anyLabelDiffersTo(
            List<LabelledWithConfidence<T>> elements, String labelToCompare) {
        return elements.stream()
                .map(LabelledWithConfidence::getLabel)
                .anyMatch(label -> !labelToCompare.equals(label));
    }

    private List<WithConfidence<ObjectMask>> deriveObjects(
            List<WithConfidence<ObjectMask>> elements) throws OperationFailedException {

        BoundedList<WithConfidence<ObjectMask>> boundedList =
                BoundedList.createFromList(
                        elements, withConfidence -> withConfidence.getElement().boundingBox());
        return DeriveObjectsFromList.deriveObjects(
                boundedList, elements, minConfidence, minNumberVoxels);
    }
}
