/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.feature;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedRunnable;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.OutputSequenceStackFactory;
import org.anchoranalysis.io.generator.sequence.OutputSequenceIncrementing;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.OutputterChecked;

/**
 * Creating and exporting thumbnails.
 *
 * @author Owen Feehan
 */
class ThumbnailsWriter {

    // Generates thumbnails, lazily if needed.
    private OutputSequenceIncrementing<Stack> thumbnailOutputSequence;

    /**
     * Outputs a thumbnail to the file-system.
     *
     * <p>Part of this operation occurs immediately (so that it can be in a synchronized block), and
     * the remainder is returned as a runnable to be executed later (which can be outside the
     * synchronized block).
     *
     * @param thumbnail the thumbnail to maybe output.
     * @param outputter the outputter.
     * @param outputName the name to use when outputting.
     * @return a runnable, that when called, completes the remain part of the outputting operation.
     * @throws OperationFailedException if the thumbnail cannot be successfully outputted.
     */
    public CheckedRunnable<OutputWriteFailedException> outputThumbnail(
            DisplayStack thumbnail, OutputterChecked outputter, String outputName)
            throws OperationFailedException {
        try {
            if (thumbnailOutputSequence == null) {
                OutputSequenceStackFactory factory = OutputSequenceStackFactory.always2D();
                thumbnailOutputSequence = factory.incrementingByOne(outputName, outputter);
            }
            return thumbnailOutputSequence.addAsynchronously(thumbnail.deriveStack(false));
        } catch (OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Deletes the stored thumbnails from memory. */
    public void removeStoredThumbnails() {
        thumbnailOutputSequence = null;
    }
}
