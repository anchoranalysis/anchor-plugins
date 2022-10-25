/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.stack;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalListParallel;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.math.arithmetic.Counter;
import org.anchoranalysis.plugin.image.task.bean.combine.Montage;
import org.anchoranalysis.plugin.image.task.size.SizeMapping;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Reads the size of each image initially, before normal (parallel) task execution.
 *
 * <p>It first attempts to do this through a {@code imageMetadataReader} as often this is much
 * faster than opening the stack with {@link StackReader}.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class ImageSizePrereader {

    // START REQUIRED ARGUMENTS
    /** How to read the {@link ImageMetadata} from the file-system. */
    private final ImageMetadataReader imageMetadataReader;

    /**
     * Fallback for {@code imageMetadataReader} to read image files without a directy metadata
     * reader.
     */
    private final StackReader stackReader;

    /** Logging and execution time recording. */
    private final OperationContext context;
    // END REQUIRED ARGUMENTS

    /**
     * Extract the image-size for each {@link StackSequenceInput}.
     *
     * @param inputs the input images.
     * @return a newly created list of the image-sizes for each input. This may not be the size size
     *     as {@code paths}, as if an error occurs, the element is dropped.
     */
    public List<SizeMapping> imageSizesFor(List<StackSequenceInput> inputs) {
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
    private Optional<SizeMapping> sizeFromInput(
            StackSequenceInput input, Counter counter, int numberInputs) {
        Optional<Extent> size = Optional.empty();
        try {
            Path path = input.pathForBindingRequired();

            // Assign, as it is checked in the final log for success
            size =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime(
                                    "Prereading image size", () -> scaledSizeFor(path));
            return size.map(extent -> new SizeMapping(path, extent));
        } catch (InputReadFailedException e) {
            context.getLogger()
                    .errorReporter()
                    .recordError(
                            ImageSizePrereader.class,
                            "Cannot determine input path: " + input.identifier());
            return Optional.empty();
        } finally {
            int index;
            synchronized (this) {
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
    private Optional<Extent> scaledSizeFor(Path imagePath) {
        try {
            ImageMetadata metadata = imageMetadataReader.openFile(imagePath, stackReader, context);
            return Optional.of(metadata.getDimensions().extent().flattenZ());
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
