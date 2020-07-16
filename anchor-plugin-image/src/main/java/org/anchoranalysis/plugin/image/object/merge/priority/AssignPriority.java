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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;
import org.anchoranalysis.plugin.image.object.merge.GraphLogger;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

public abstract class AssignPriority {

    /**
     * Assigns a priority to a potential merge
     *
     * @param src source (first) object in the pair of objects that could be merged
     * @param dest destination (second) object in the pair of objects that could be merged
     * @param merge merged-object
     * @param payloadCalculator calculates the payload for any object
     * @param logger logger
     * @return the object with a priority afforded
     * @throws OperationFailedException
     */
    public PrioritisedVertex assignPriority(ObjectVertex src, ObjectVertex dest, GraphLogger logger)
            throws OperationFailedException {

        // Do merge
        ObjectMask merge = ObjectMaskMerger.merge(src.getObject(), dest.getObject());

        PrioritisedVertex withPriority =
                assignPriorityToEdge(src, dest, merge, logger.getErrorReporter());

        logger.describeEdge(
                src,
                dest,
                withPriority.getOmWithFeature(),
                withPriority.getPriority(),
                withPriority.isConsiderForMerge());

        return withPriority;
    }

    protected abstract PrioritisedVertex assignPriorityToEdge(
            ObjectVertex src, ObjectVertex dest, ObjectMask merge, ErrorReporter errorReporter)
            throws OperationFailedException;
}
