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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.LabelledWithConfidence;

/**
 * Where objects overlap, they are merged together if certain conditions are fulfilled.
 *
 * <p>Conditions:
 *
 * <ul>
 *   <li>if the number of voxels in the lower-confidence element is small (relative to the
 *       higher-confidence object) <b>or</b>
 *   <li>if the confidence of the two elements is similar, then the overlapping element is merged
 *       into the source element, and adopts its confidence.
 * </ul>
 *
 * <p>Otherwise, the overlapping element remains in the list to be reduced (with whatever change to
 * its voxels).
 *
 * <p>This achieves a similar result as {@link ThresholdConfidence} but is typically slower (unless
 * there are very few overlapping objects). However, it offers a greater ability to distinguish
 * overlapping objects of significantly differing confidence.
 *
 * @author Owen Feehan
 */
public class ConditionallyMergeOverlappingObjects extends ReduceElementsGreedy {

    // START BEAN PROPERTIES
    /**
     * The maximum size for merging. If a clipped-object has fewer voxels than this (relative to the
     * source object) it is always merged.
     */
    @BeanField @Getter @Setter private double overlapThreshold = 0.2;

    /**
     * The maximum difference in confidence for merging. If a clipped-object has a lower difference
     * in confidence than this, it is always merged.
     */
    @BeanField @Getter @Setter private double confidenceThreshold = 0.1;
    // END BEAN PROPERTIES

    @Override
    protected boolean shouldObjectsBeProcessed(ObjectMask source, ObjectMask other) {
        // Include any object that overlaps
        return true;
    }

    @Override
    protected boolean processObjects(
            LabelledWithConfidence<ObjectMask> source,
            LabelledWithConfidence<ObjectMask> other,
            ReduceObjectsGraph graph) {

        removeCommonVoxelsFromOther(source, other);

        // Remove the edge from the graph that indicates overlap is present
        graph.removeEdge(source, other);

        // The number of overlapping pixels
        int overlap = other.getElement().numberVoxelsOn();

        // If there are no longer any voxels left on the object, it is removed from consideration.
        if (overlap == 0) {
            removeVertex(graph, other);
            return true;
        } else if (shouldMerge(overlap, source, other)) {
            return mergeObjects(source, other, graph);
        } else {
            // NOTHING TO DO. The object (clipped) remains as an element, but with any overlapping
            // voxels removed.
            return false;
        }
    }

    /** Whethere two object-masks should be merged together? */
    private boolean shouldMerge(
            int numberVoxelsOverlapping,
            LabelledWithConfidence<ObjectMask> source,
            LabelledWithConfidence<ObjectMask> other) {
        double overlapScore =
                ScoreHelper.overlapScore(numberVoxelsOverlapping, source.getElement());
        return (overlapScore < overlapThreshold)
                || ScoreHelper.confidenceDifference(source, other) < confidenceThreshold;
    }

    /** Removes any common voxels (between source and other) from other. */
    private static void removeCommonVoxelsFromOther(
            LabelledWithConfidence<ObjectMask> source, LabelledWithConfidence<ObjectMask> other) {
        other.getElement().assignOff().toObject(source.getElement());
    }

    /** Removes a vertex from the graph. */
    private static void removeVertex(
            ReduceObjectsGraph graph, LabelledWithConfidence<ObjectMask> vertex) {
        try {
            graph.removeVertex(vertex);
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /** Merges two objects together. */
    private static boolean mergeObjects(
            LabelledWithConfidence<ObjectMask> source,
            LabelledWithConfidence<ObjectMask> other,
            ReduceObjectsGraph graph) {
        ObjectMask merged = ObjectMaskMerger.merge(source.getElement(), other.getElement());
        // Use the confidence from the source
        try {
            LabelledWithConfidence<ObjectMask> mergedWithConfidence =
                    new LabelledWithConfidence<>(merged, source.getConfidence(), source.getLabel());
            graph.mergeVertices(source, other, mergedWithConfidence);
            return true;
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
