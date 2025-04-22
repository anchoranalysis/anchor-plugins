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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.inference.bean.segment.reduce.ReduceElements;
import org.anchoranalysis.image.inference.segment.LabelledWithConfidence;
import org.anchoranalysis.image.inference.segment.ReductionOutcome;
import org.anchoranalysis.image.inference.segment.WithConfidence;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.BoundingBoxMerger;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.rtree.SpatiallySeparate;

/**
 * Combines object-masks by projecting the maximum confidence-level for each voxel and thresholding.
 *
 * <p>After thresholding, a connected-components algorithm splits the thresholded-mask into
 * single-objects.
 *
 * <p>Depending on the number of objects, two types of projection occur:
 *
 * <ul>
 *   <li>the projection occurs globally on an identically sized image to the entire size (more
 *       efficient for a larger number of objects and/or small image), or
 *   <li>using an R-Tree clusters of intersecting boxes are found, and each is processed separately
 *       (more efficient for a smaller number of objects and/or large image).
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class ThresholdConfidence extends ReduceElements<ObjectMask> {

    // START BEAN PROPERTIES
    /**
     * The minimum confidence of an element for its object-mask to be <b>initially</b> included for
     * consideration (before merging).
     */
    @BeanField @Getter @Setter private double minConfidence = 0.5;

    /** The minimum number of voxels that must exist in a connected-component to be included. */
    @BeanField @Getter @Setter private int minNumberVoxels = 5;

    /**
     * When the number of objects is greater or equal than this, they are reduced globally, without
     * separation. See class javadoc.
     */
    @BeanField @Getter @Setter private int thresholdNumberObjectsGlobal = 20;

    // END BEAN PROPERTIES

    /**
     * Creates with a minimum-confidence level.
     *
     * @param minConfidence the minimum confidence of an element for its object-mask to be
     *     <b>finally</b> included (after merging).
     */
    public ThresholdConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    @Override
    public ReductionOutcome<LabelledWithConfidence<ObjectMask>> reduce(
            List<LabelledWithConfidence<ObjectMask>> elements,
            Extent extent,
            ExecutionTimeRecorder executionTimeRecorder)
            throws OperationFailedException {

        // Filter
        List<LabelledWithConfidence<ObjectMask>> elementsFiltered =
                elements.stream()
                        .filter(withConfidence -> withConfidence.getConfidence() >= minConfidence)
                        .toList();

        if (elementsFiltered.isEmpty()) {
            // An empty input list produces an outcome where no elements are retained (because none
            // exist).
            return new ReductionOutcome<>();
        }

        // Take the label from the first element
        String label = elementsFiltered.get(0).getLabel();

        // Check that all other labels are the same, otherwise we cannot proceed
        if (anyLabelDiffersTo(elementsFiltered, label)) {
            throw new OperationFailedException(
                    "Labels are not all identical, so this reduction operation cannot proceed.");
        }

        ReductionOutcome<LabelledWithConfidence<ObjectMask>> outcome = new ReductionOutcome<>();

        executionTimeRecorder.recordExecutionTime(
                "Derive labelled reduced objects",
                () ->
                        projectAllObjects(
                                elementsFiltered,
                                extent,
                                withConfidence ->
                                        outcome.addNewlyAdded(
                                                new LabelledWithConfidence<>(
                                                        label, withConfidence)),
                                executionTimeRecorder));

        return outcome;
    }

    /** Perform the projection on all {@code elements} using either of the two methods. */
    private void projectAllObjects(
            List<LabelledWithConfidence<ObjectMask>> elements,
            Extent extent,
            Consumer<WithConfidence<ObjectMask>> addToOutcome,
            ExecutionTimeRecorder executionTimeRecorder)
            throws OperationFailedException {
        if (elements.size() >= thresholdNumberObjectsGlobal) {
            // If there are many elements, we prefer to use a raster on the entire scene, as
            // "separating"
            // the elements into clusters can be expensive
            deriveObjects(elements.stream(), new BoundingBox(extent), addToOutcome);
        } else {
            // If there are few elements, we separate the elements, to avoid having to create a big
            // raster for small areas of space.
            projectSeparatedObjects(elements, addToOutcome, executionTimeRecorder);
        }
    }

    /** Perform the projection on all {@code elements} after separating them via a R-Tree. */
    private void projectSeparatedObjects(
            List<LabelledWithConfidence<ObjectMask>> elements,
            Consumer<WithConfidence<ObjectMask>> addToOutcome,
            ExecutionTimeRecorder executionTimeRecorder)
            throws OperationFailedException {
        SpatiallySeparate<LabelledWithConfidence<ObjectMask>> separate =
                new SpatiallySeparate<>(
                        withConfidence -> withConfidence.getElement().boundingBox());

        List<Set<LabelledWithConfidence<ObjectMask>>> separatedElements =
                executionTimeRecorder.recordExecutionTime(
                        "Spatially separate for reduction", () -> separate.separate(elements));

        // For efficiency on rasters sparsely populated with objects, process each
        //  spatially-connected set of objects separately.
        for (Set<LabelledWithConfidence<ObjectMask>> split : separatedElements) {
            BoundingBox mergedBox =
                    BoundingBoxMerger.merge(
                            split.stream()
                                    .map(
                                            withConfidence ->
                                                    withConfidence.getElement().boundingBox()));
            deriveObjects(split.stream(), mergedBox, addToOutcome);
        }
    }

    /**
     * Projects elements into a raster, and derives a list of {@link ObjectMask}s from the
     * connected-components.
     */
    private void deriveObjects(
            Stream<LabelledWithConfidence<ObjectMask>> elements,
            BoundingBox box,
            Consumer<WithConfidence<ObjectMask>> addToOutcome)
            throws OperationFailedException {
        DeriveObjectsFromStream.deriveObjects(
                        elements.map(LabelledWithConfidence::getWithConfidence),
                        box,
                        minConfidence,
                        minNumberVoxels)
                .stream()
                .forEach(addToOutcome);
    }

    /** Checks if any label is different to {@code labelToCompare}. */
    private static <T> boolean anyLabelDiffersTo(
            List<LabelledWithConfidence<T>> elements, String labelToCompare) {
        return elements.stream()
                .map(LabelledWithConfidence::getLabel)
                .anyMatch(label -> !labelToCompare.equals(label));
    }
}
