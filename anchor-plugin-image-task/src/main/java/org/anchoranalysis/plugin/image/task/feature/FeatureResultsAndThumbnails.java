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
package org.anchoranalysis.plugin.image.task.feature;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedRunnable;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.feature.io.csv.results.FeatureCSVWriterFactory;
import org.anchoranalysis.feature.io.results.FeatureOutputMetadata;
import org.anchoranalysis.feature.io.results.LabelledResultsCollector;
import org.anchoranalysis.feature.io.results.LabelledResultsVector;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.io.output.enabled.multi.MultiLevelOutputEnabled;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.OutputterChecked;

/**
 * A collection of results from feature calculation, and associated thumbnails.
 *
 * <p>It supports concurrent access by multiple threads.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>thumbnails</td><td>yes</td><td>a small picture for each row in the {@code features} CSV illustrating its content.</td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class FeatureResultsAndThumbnails {

    private static final String OUTPUT_THUMBNAILS = FeatureExporter.OUTPUT_THUMBNAILS;

    /** Where the results of feature-calculation are stored/outputted. */
    private final LabelledResultsCollector results;

    /** Outputs thumbnails. */
    private ThumbnailsWriter thumbnails = new ThumbnailsWriter();

    private final OutputterChecked outputter;

    /**
     * Whether the calculation of the feature-results is needed. If only thumbnails are being
     * written, the calculation of results can be skipped.
     */
    private final boolean calculationResultsNeeded;

    /** Whether thumbnails are being calculated or not. */
    private final boolean thumbnailsEnabled;

    private final FeatureExporterContext context;

    private final FeatureOutputMetadata outputMetadata;

    public FeatureResultsAndThumbnails(
            FeatureOutputMetadata outputMetadata, FeatureExporterContext context)
            throws OutputWriteFailedException {
        this.outputMetadata = outputMetadata;
        this.results =
                new WriteWithGroups(
                        outputMetadata.csvNonAggregated(),
                        context::csvWriter,
                        context.isRemoveNaNColumns());
        this.outputter = context.getContext().getOutputter().getChecked();
        MultiLevelOutputEnabled outputEnabled =
                context.getContext().getOutputter().getChecked().getOutputsEnabled();
        this.calculationResultsNeeded =
                outputMetadata.outputNames().calculationResultsNeeded(outputEnabled);
        this.thumbnailsEnabled = outputEnabled.isOutputEnabled(OUTPUT_THUMBNAILS);
        this.context = context;
    }

    /**
     * Adds results, together with a thumbnail.
     *
     * <p>Note that in order for the sequence numbers to match the order in the feature table, a
     * thumbnail should be present for <i>all</i> results, or for <i>no</i> results, but never
     * partially.
     *
     * <p>The results are only calculated if any of the relevant outputs are enabled. Similarly with
     * the thumbnails.
     *
     * @param resultToAdd supplies the results to add, which may or may not be called, depending on
     *     what outputs are enabled.
     * @param thumbnail supplies the thumbnail to write, which may or may not be called, depending
     *     on what outputs are enabled.
     * @throws OperationFailedException if the operation cannot be successfully completed.
     */
    public void add(
            CheckedSupplier<LabelledResultsVector, OperationFailedException> resultToAdd,
            CheckedSupplier<Optional<DisplayStack>, OperationFailedException> thumbnail)
            throws OperationFailedException {

        ExecutionTimeRecorder recorder = context.getContext().getExecutionTimeRecorder();

        // Synchronization is important to preserve identical order in thumbnails and in the
        // exported feature CSV file, as multiple threads will concurrently add results.
        LabelledResultsVector labelledResult =
                calculationResultsNeeded
                        ? recorder.recordExecutionTime(
                                "Calculate labelled results", resultToAdd::get)
                        : null;

        Optional<DisplayStack> thumbnailStack =
                thumbnailsEnabled
                        ? recorder.recordExecutionTime("Thumbnail for input", thumbnail::get)
                        : Optional.empty();

        // Adding to the results map and writing the thumbnail need to happen together in the same
        // synchronized block
        //  so as to achieve the correct output order. However, we try to move the bulk of the
        // computation outside this
        //  block to stop concurrent threads waiting needlessly. The thumbnail writing I/O remains
        // inside the synchronized
        //  block.
        CheckedRunnable<OutputWriteFailedException> thumbnailOutputter = null;
        synchronized (this) {
            // Add results to grouped-map, and write to CSV file
            if (calculationResultsNeeded) {
                recorder.recordExecutionTime("Writing CSV", () -> results.add(labelledResult));
            }

            if (thumbnailsEnabled && thumbnailStack.isPresent()) {
                // Perform the first part of the thumbnail outputting operation, deferring the
                // second part
                // until we are outside the synchronized block.
                thumbnailOutputter =
                        thumbnails.outputThumbnail(
                                thumbnailStack.get(), outputter, OUTPUT_THUMBNAILS);
            }
        }

        // Perform the second part of the thumbnail outputting operation (outside the synchronized
        // block).
        if (thumbnailOutputter != null) {
            try {
                recorder.recordExecutionTime("Writing thumbnail", thumbnailOutputter::run);
            } catch (OutputWriteFailedException e) {
                throw new OperationFailedException(e);
            }
        }
    }

    /**
     * Writes all the results that have been collected as a CSV file.
     *
     * @param featuresAggregate features that can be used for generating additional "aggregated"
     *     exports.
     * @param includeGroups iff true a group-column is included in the CSV file and the group
     *     exports occur, otherwise not.
     * @param csvWriterCreator creates a CSV writer for a particular IO-context.
     * @param context IO-context.
     * @throws OutputWriteFailedException
     */
    public void writeGroupedResults(
            Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
            boolean includeGroups,
            Function<InputOutputContext, FeatureCSVWriterFactory> csvWriterCreator,
            InputOutputContext context)
            throws OutputWriteFailedException {
        synchronized (this) {
            results.flushAndClose(
                    featuresAggregate, includeGroups, csvWriterCreator, outputMetadata, context);
        }
    }

    /**
     * Closes any open IO and removes redundant structures stored in memory.
     *
     * @throws IOException if the close operation cannot successfully complete.
     */
    public void closeAnyOpenIO() throws IOException {
        synchronized (this) {
            thumbnails.removeStoredThumbnails();
        }
    }
}
