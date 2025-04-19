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

package org.anchoranalysis.plugin.image.object.merge.condition;

import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * A condition to be checked after merging two {@link ObjectMask}s.
 */
public interface AfterCondition {

    /**
     * Initializes the condition with a logger.
     *
     * @param logger the {@link Logger} to be used for logging
     * @throws InitializeException if initialization fails
     */
    void initialize(Logger logger) throws InitializeException;

    /**
     * Checks if the merged object satisfies the condition.
     *
     * @param source the source {@link ObjectMask} that was merged
     * @param destination the destination {@link ObjectMask} that was merged into
     * @param merged the resulting merged {@link ObjectMask}
     * @return true if the merged object satisfies the condition, false otherwise
     * @throws OperationFailedException if the condition check fails
     */
    boolean accept(ObjectMask source, ObjectMask destination, ObjectMask merged)
            throws OperationFailedException;
}