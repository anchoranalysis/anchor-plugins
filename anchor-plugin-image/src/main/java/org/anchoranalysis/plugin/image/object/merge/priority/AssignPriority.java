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

package org.anchoranalysis.plugin.image.object.merge.priority;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.GraphLogger;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/**
 * Abstract class for assigning priorities to potential object merges.
 */
public abstract class AssignPriority {

    /**
     * Assigns a priority to a potential merge between two objects.
     *
     * @param source the source (first) {@link ObjectVertex} in the pair of objects that could be merged
     * @param destination the destination (second) {@link ObjectVertex} in the pair of objects that could be merged
     * @param logger the {@link GraphLogger} for logging merge operations
     * @return a {@link PrioritisedVertex} with an assigned priority
     * @throws OperationFailedException if the priority assignment fails
     */
    public PrioritisedVertex assignPriority(
            ObjectVertex source, ObjectVertex destination, GraphLogger logger)
            throws OperationFailedException {

        // Do merge
        ObjectMask merge = ObjectMaskMerger.merge(source.getObject(), destination.getObject());

        PrioritisedVertex withPriority =
                assignPriorityToEdge(source, destination, merge, logger.getErrorReporter());

        logger.describeEdge(
                source,
                destination,
                withPriority.getVertex(),
                withPriority.getPriority(),
                withPriority.isConsiderForMerge());

        return withPriority;
    }

    /**
     * Assigns a priority to an edge between two objects.
     *
     * @param source the source {@link ObjectVertex}
     * @param destination the destination {@link ObjectVertex}
     * @param merge the merged {@link ObjectMask}
     * @param errorReporter the {@link ErrorReporter} for reporting errors
     * @return a {@link PrioritisedVertex} with an assigned priority
     * @throws OperationFailedException if the priority assignment fails
     */
    protected abstract PrioritisedVertex assignPriorityToEdge(
            ObjectVertex source,
            ObjectVertex destination,
            ObjectMask merge,
            ErrorReporter errorReporter)
            throws OperationFailedException;
}