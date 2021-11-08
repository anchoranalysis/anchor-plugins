package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
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

    private static final String MANIFEST_FUNCTION_THUMBNAIL = "thumbnail";

    // Generates thumbnails, lazily if needed.
    private OutputSequenceIncrementing<Stack> thumbnailOutputSequence;

    /**
     * Outputs a thumbnail if it exists, and the outputter allows it.
     *
     * @param thumbnail the thumbnail to maybe output.
     * @param outputter the outputter.
     * @param outputName the name to use when outputting.
     * @throws OperationFailedException if the thumbnail cannot be successfully outputted.
     */
    public void maybeOutputThumbnail(
            Optional<DisplayStack> thumbnail, OutputterChecked outputter, String outputName)
            throws OperationFailedException {
        try {
            if (thumbnail.isPresent()) {
                outputThumbnail(thumbnail.get(), outputter, outputName);
            }
        } catch (OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Deletes the stored thumbnails from memory. */
    public void removeStoredThumbnails() {
        thumbnailOutputSequence = null;
    }

    /** Outputs a thumbnail to the file-system. */
    private void outputThumbnail(
            DisplayStack thumbnail, OutputterChecked outputter, String outputName)
            throws OutputWriteFailedException {
        if (thumbnailOutputSequence == null) {
            OutputSequenceStackFactory factory =
                    OutputSequenceStackFactory.always2D(MANIFEST_FUNCTION_THUMBNAIL);
            thumbnailOutputSequence = factory.incrementingByOne(outputName, outputter);
        }
        thumbnailOutputSequence.add(thumbnail.deriveStack(false));
    }
}
