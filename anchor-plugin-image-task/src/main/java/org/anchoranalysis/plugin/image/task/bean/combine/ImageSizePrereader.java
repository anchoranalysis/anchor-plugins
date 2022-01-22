package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalListParallel;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.math.arithmetic.Counter;
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
        context.getLogger()
                .messageLogger()
                .logFormatted(
                        "Prereading %d images in parallel across multiple cores", inputs.size());
        Counter counter = new Counter();
        return FunctionalListParallel.mapToListOptional(
                inputs,
                ExperimentExecutionException.class,
                input -> sizeFromInput(input, counter, inputs.size()));
    }

    /**
     * Derives an {@link Extent} for a particular {@link StackSequenceInput}, returning it with its
     * associated path.
     */
    private Optional<Pair<Path, Extent>> sizeFromInput(
            StackSequenceInput input, Counter counter, int numberInputs)
            throws ExperimentExecutionException {
        Optional<Extent> size = Optional.empty();
        try {
            Path path = input.pathForBindingRequired();

            // Assign, as it is checked in the final log for success
            size =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime(
                                    "Prereading image size", () -> scaledSizeFor(path));
            return size.map(extent -> new Pair<>(path, extent));
        } catch (InputReadFailedException e) {
            context.getLogger()
                    .errorReporter()
                    .recordError(
                            ImageSizePrereader.class,
                            "Cannot determine input path: " + input.identifier());
            return Optional.empty();
        } finally {
            int index;
            synchronized (counter) {
                index = counter.getCount();
                counter.increment();
            }
            context.getLogger()
                    .messageLogger()
                    .logFormatted(
                            "Pre-reading image size (%d of %d) %s: %s",
                            index,
                            numberInputs,
                            size.isPresent() ? "succeeded" : "  failed",
                            input.identifier());
        }
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
            return Optional.of(
                    metadata.getDimensions().extent().flattenZ().scaleXYBy(factor, true));
        } catch (Exception e) {
            // Catch all exceptions, and give up after recording an error in the log.
            context.getLogger()
                    .errorReporter()
                    .recordError(
                            Montage.class,
                            new ExperimentExecutionException(
                                    "Cannot read the image-metadata for file at: " + imagePath, e));
            return Optional.empty();
        }
    }
}
