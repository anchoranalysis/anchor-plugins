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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.condition.AfterCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.BeforeCondition;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3d;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * Naive merge algorithm that merges in a very greedy away as long as certain conditions are
 * fulfilled.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class NaiveGreedyMerge {

    /** If true, replaces merged objects with their midpoint. */
    private final boolean replaceWithMidpoint;

    /** Condition to check before merging objects. */
    private final BeforeCondition beforeCondition;

    /** Condition to check after merging objects. */
    private final AfterCondition afterCondition;

    /** Optional unit converter for distance calculations. */
    private final Optional<UnitConverter> unitConverter;

    /** Logger for outputting messages. */
    private final Logger logger;

    /** Represents a range of indices for merging. */
    @Value
    private static class MergeRange {
        private int start; // NOSONAR
        private int end; // NOSONAR
    }

    /**
     * Tries to merge objects (the collection is changed in-place).
     *
     * @param objects the {@link ObjectCollection} to merge
     * @return the merged {@link ObjectCollection}
     * @throws OperationFailedException if the merge operation fails
     */
    public ObjectCollection tryMerge(ObjectCollection objects) throws OperationFailedException {

        // Stack structure, last in, first out
        Deque<MergeRange> stack = new ArrayDeque<>();

        stack.add(new MergeRange(0, 0));

        while (!stack.isEmpty()) {
            tryMergeWithinRange(objects, stack.pop(), stack::push);
        }

        return objects;
    }

    /**
     * Tries to merge a particular subset of objects in objects based upon the current range.
     *
     * @param objects the entire set of objects
     * @param range parameters that determine which objects are considered for merge
     * @param consumer consumer for new merge ranges
     * @throws OperationFailedException if the merge operation fails
     */
    private void tryMergeWithinRange(
            ObjectCollection objects, MergeRange range, Consumer<MergeRange> consumer)
            throws OperationFailedException {

        try {
            afterCondition.initialize(logger);
        } catch (InitializeException e) {
            throw new OperationFailedException(e);
        }

        for (int i = range.getStart(); i < objects.size(); i++) {
            for (int j = range.getEnd(); j < objects.size(); j++) {

                if (i != j && tryMergeOnIndices(objects.asList(), i, j, consumer)) {
                    // After a successful merge, we don't try to merge again
                    break;
                }
            }
        }
    }

    /**
     * Tries to merge two objects at specific indices.
     *
     * @param objects the list of {@link ObjectMask}s
     * @param i index of the first object
     * @param j index of the second object
     * @param consumer consumer for new merge ranges
     * @return true if a merge occurred, false otherwise
     * @throws OperationFailedException if the merge operation fails
     */
    private boolean tryMergeOnIndices(
            List<ObjectMask> objects, int i, int j, Consumer<MergeRange> consumer)
            throws OperationFailedException {
        Optional<ObjectMask> merged = tryMerge(objects.get(i), objects.get(j));
        if (merged.isPresent()) {
            removeTwoIndices(objects, i, j);
            objects.add(merged.get());

            int startPos = Math.max(i - 1, 0);
            consumer.accept(new MergeRange(startPos, startPos));

            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to merge two {@link ObjectMask}s.
     *
     * @param source the source {@link ObjectMask}
     * @param destination the destination {@link ObjectMask}
     * @return an {@link Optional} containing the merged {@link ObjectMask} if successful, empty
     *     otherwise
     * @throws OperationFailedException if the merge operation fails
     */
    private Optional<ObjectMask> tryMerge(ObjectMask source, ObjectMask destination)
            throws OperationFailedException {

        if (!beforeCondition.accept(source, destination, unitConverter)) {
            return Optional.empty();
        }

        // Do merge
        ObjectMask merged = merge(source, destination);

        if (!afterCondition.accept(source, destination, merged)) {
            return Optional.empty();
        }

        return Optional.of(merged);
    }

    /**
     * Merges two {@link ObjectMask}s.
     *
     * @param source the source {@link ObjectMask}
     * @param destination the destination {@link ObjectMask}
     * @return the merged {@link ObjectMask}
     */
    private ObjectMask merge(ObjectMask source, ObjectMask destination) {
        if (replaceWithMidpoint) {
            Point3i pointNew =
                    PointConverter.intFromDoubleFloor(
                            midPointBetween(
                                    source.boundingBox().midpoint(),
                                    destination.boundingBox().midpoint()));
            return createSinglePixelObject(pointNew);
        } else {
            return ObjectMaskMerger.merge(source, destination);
        }
    }

    /**
     * Creates a single-pixel {@link ObjectMask} at the specified point.
     *
     * @param point the {@link Point3i} where the single-pixel object should be created
     * @return a new {@link ObjectMask} representing a single pixel
     */
    private static ObjectMask createSinglePixelObject(Point3i point) {
        Extent extent = new Extent(1, 1, 1);
        BinaryVoxels<UnsignedByteBuffer> voxels = BinaryVoxelsFactory.createEmptyOn(extent);
        return new ObjectMask(BoundingBox.createReuse(point, extent), voxels);
    }

    /**
     * Removes two objects from the list at the specified indices.
     *
     * @param objects the list of {@link ObjectMask}s
     * @param i index of the first object to remove
     * @param j index of the second object to remove
     */
    private static void removeTwoIndices(List<ObjectMask> objects, int i, int j) {
        if (i < j) {
            objects.remove(j);
            objects.remove(i);
        } else {
            objects.remove(i);
            objects.remove(j);
        }
    }

    /**
     * Calculates the midpoint between two 3D points.
     *
     * @param point1 the first {@link Point3d}
     * @param point2 the second {@link Point3d}
     * @return a new {@link Point3d} representing the midpoint between point1 and point2
     */
    private static Point3d midPointBetween(Point3d point1, Point3d point2) {
        // We create a new object of 1x1x1 between the two merged seeds
        Point3d pointNew = new Point3d(point1);
        pointNew.add(point2);
        pointNew.scale(0.5);
        return pointNew;
    }
}
