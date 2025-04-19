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

package org.anchoranalysis.plugin.image.object.merge;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.graph.TypedEdge;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.plugin.image.object.merge.priority.PrioritisedVertex;

/** A logger for graph operations, using a {@link DescribeGraph} to generate descriptions. */
@AllArgsConstructor
public class GraphLogger {

    /** The {@link DescribeGraph} used to generate descriptions of graph elements. */
    private DescribeGraph describeGraph;

    /** The {@link Logger} used to output messages. */
    private Logger logger;

    /**
     * Logs a description of a potential edge merge.
     *
     * @param src the source {@link ObjectVertex}
     * @param dest the destination {@link ObjectVertex}
     * @param merged the resulting merged {@link ObjectVertex}
     * @param priority the priority of the merge
     * @param doMerge whether the merge will be performed
     */
    public void describeEdge(
            ObjectVertex src,
            ObjectVertex dest,
            ObjectVertex merged,
            double priority,
            boolean doMerge) {
        log(describeGraph.describeEdge(src, dest, merged, priority, doMerge));
    }

    /**
     * Logs a description of a merge operation that has been decided upon.
     *
     * @param omMerged the resulting merged {@link ObjectVertex}
     * @param bestImprovement the {@link TypedEdge} representing the best improvement for merging
     */
    public void describeMerge(
            ObjectVertex omMerged, TypedEdge<ObjectVertex, PrioritisedVertex> bestImprovement) {
        log(describeGraph.describeMerge(omMerged, bestImprovement));
    }

    /** Logs a description of the entire graph. */
    public void logDescription() {
        log(describeGraph.describe());
    }

    /**
     * Gets the error reporter associated with the logger.
     *
     * @return the {@link ErrorReporter} for reporting errors
     */
    public ErrorReporter getErrorReporter() {
        return logger.errorReporter();
    }

    /**
     * Logs a message using the internal logger.
     *
     * @param str the message to log
     */
    private void log(String str) {
        logger.messageLogger().log(str);
    }
}
