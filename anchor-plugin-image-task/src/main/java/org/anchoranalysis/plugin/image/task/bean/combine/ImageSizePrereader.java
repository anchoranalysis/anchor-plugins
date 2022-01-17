package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.apache.commons.math3.util.Pair;

/**
 * Reads the size of each image initially, before normal (parallel) task execution.
 *
 * <p>It first attempts to do this through a {@code imageMetadataReader} as often this is much
 * faster than opening the stack with {@link StackReader}.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class ImageSizePrereader {

    // START REQUIRED ARGUMENTS
    /** How much to scale each image by, before fitting to the montage. */
    private final ScaleCalculator scale;

    /** How to read the {@link ImageMetadata} from the file-system. */
    private final ImageMetadataReader imageMetadataReader;

    /**
     * Fallback for {@code imageMetadataReader} to read image files without a directy metadata
     * reader.
     */
    private final StackReader stackReader;

    /** Suggestion on how to scale the images. */
    private final Optional<ImageSizeSuggestion> suggestedResize;

    /** Logging and execution time recording. */
    private final OperationContext context;
    // END REQUIRED ARGUMENTS

    /**
     * Extract a {@link Extent} for each {@link Path} representing an input image.
     *
     * @param inputs the input images.
     * @return a newly created list of the derived {@link Extent} for each selected path. This may
     *     not be the size size as {@code paths}, as if an error occurs, the element is dropped.
     */
    public List<Pair<Path, Extent>> deriveSizeForAllInputs(List<StackSequenceInput> inputs)
            throws ExperimentExecutionException {
        return context.getExecutionTimeRecorder()
                .recordExecutionTime(
                        "Deriving initially the size of each image", () -> deriveSizes(inputs));
    }

    /**
     * Extract a {@link Extent} for each {@link Path} representing an input image, without recording
     * execution-time.
     */
    private List<Pair<Path, Extent>> deriveSizes(List<StackSequenceInput> inputs)
            throws ExperimentExecutionException {
        List<Pair<Path, Extent>> out = new LinkedList<>();
        int index = 1;
        for(StackSequenceInput input : inputs) {

            Optional<Extent> size = Optional.empty();
            try {
                Path path = input.pathForBindingRequired();

                size = scaledSizeFor(path);
                if (size.isPresent()) {
                    out.add(new Pair<>(path, size.get()));
                }
            } catch (InputReadFailedException e) {
                context.getLogger()
                        .errorReporter()
                        .recordError(
                                ImageSizePrereader.class,
                                "Cannot determine input path: " + input.identifier());
            }

            context.getLogger()
                    .messageLogger()
                    .logFormatted(
                            "Pre-reading image size (%d of %d) %s: %s",
                            index++,
                            inputs.size(),
                            size.isPresent() ? "succeeded" : "  failed",
                            input.identifier());
        }
        return out;
    }

    /**
     * Determines the scaled size for a particular image, which is used to populate the table. The
     * final size will be approximately similar, but not necessarily identical.
     *
     * <p>The size is read from the file-system from a {@code imageMetadataReader} as this often is
     * much quicker than reading the entire raster (which will occur later in parallel when actually
     * reading the image).
     */
    private Optional<Extent> scaledSizeFor(Path imagePath) throws ExperimentExecutionException {
        try {
            ImageMetadata metadata = imageMetadataReader.openFile(imagePath, stackReader, context);
            ScaleFactor factor =
                    scale.calculate(Optional.of(metadata.getDimensions()), suggestedResize);
            return Optional.of(metadata.getDimensions().extent().scaleXYBy(factor, true));
        } catch (ImageIOException e) {
            context.getLogger()
                    .errorReporter()
                    .recordError(
                            Montage.class,
                            new ExperimentExecutionException(
                                    "Cannot read the image-metadata for file at: " + imagePath, e));
            return Optional.empty();
        } catch (OperationFailedException e) {
            context.getLogger()
                    .errorReporter()
                    .recordError(
                            Montage.class,
                            new ExperimentExecutionException(
                                    "Cannot calculate scale for file at: " + imagePath, e));
            return Optional.empty();
        }
    }
}
