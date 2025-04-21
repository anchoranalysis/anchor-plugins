/*-
 * #%L
 * anchor-plugin-image-task
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
package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * The context in which features are calculated, so as to be later exported as a CSV.
 *
 * @param <S> encapsulation of the features that provide the calculation.
 */
public class FeatureCalculationContext<S> {

    /** The stored results and thumbnail writer. */
    @Getter private final FeatureResultsAndThumbnails results;

    /** The features that are calculated, encapsulated in some object. */
    @Getter private final S featureSource;

    /**
     * The name of each feature being calculated, in identical order, as that is placed in the
     * exported CSV.
     */
    @Getter private final FeatureNameList featureNames;

    /** The optional name of the group generator. */
    @Getter private final Optional<String> groupGeneratorName;

    /** Records the execution-time of particular operations. */
    @Getter private final ExecutionTimeRecorder executionTimeRecorder;

    /** The input-output context for the operation. */
    @Getter private final InputOutputContext context;

    /**
     * When false, an image is reported as errored, if any exception is thrown during calculation.
     * When true, then a value of {@link Double#NaN} is returned, and a message is written to the
     * error-log.
     */
    @Getter private final boolean suppressErrors;

    /** Whether thumbnails are enabled for output. */
    @Getter private boolean thumbnailsEnabled;

    /**
     * Creates a new {@link FeatureCalculationContext}.
     *
     * @param results the stored results and thumbnail writer
     * @param rowSource the source of features to be calculated
     * @param featureNames the names of features to be calculated
     * @param groupGeneratorName the optional name of the group generator
     * @param executionTimeRecorder records execution time of operations
     * @param suppressErrors if true, exceptions during calculation are suppressed
     * @param context the input-output context for the operation
     */
    public FeatureCalculationContext(
            FeatureResultsAndThumbnails results,
            S rowSource,
            FeatureNameList featureNames,
            Optional<String> groupGeneratorName,
            ExecutionTimeRecorder executionTimeRecorder,
            boolean suppressErrors,
            InputOutputContext context) {
        this.results = results;
        this.featureSource = rowSource;
        this.featureNames = featureNames;
        this.groupGeneratorName = groupGeneratorName;
        this.executionTimeRecorder = executionTimeRecorder;
        this.suppressErrors = suppressErrors;
        this.context = context;
        this.thumbnailsEnabled = areThumbnailsEnabled(context);
    }

    /**
     * Gets the model directory path.
     *
     * @return the {@link Path} to the model directory
     */
    public Path getModelDirectory() {
        return context.getModelDirectory();
    }

    /**
     * Gets the logger for the context.
     *
     * @return the {@link Logger} instance
     */
    public Logger getLogger() {
        return context.getLogger();
    }

    /**
     * Gets the operation context.
     *
     * @return the {@link OperationContext} instance
     */
    public OperationContext getOperationContext() {
        return context.operationContext();
    }

    /**
     * Determines if thumbnails are enabled for the given context.
     *
     * @param context the {@link InputOutputContext} to check
     * @return true if thumbnails are enabled, false otherwise
     */
    private boolean areThumbnailsEnabled(InputOutputContext context) {
        return context.getOutputter()
                .outputsEnabled()
                .isOutputEnabled(FeatureExporter.OUTPUT_THUMBNAILS);
    }
}
