/*-
 * #%L
 * anchor-feature-io
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

import java.util.Optional;
import java.util.function.Function;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.feature.io.csv.metadata.FeatureCSVMetadataForOutput;
import org.anchoranalysis.feature.io.csv.results.FeatureCSVWriterFactory;
import org.anchoranalysis.feature.io.csv.results.LabelledResultsCSVWriter;
import org.anchoranalysis.feature.io.csv.results.LabelledResultsCSVWriterFactory;
import org.anchoranalysis.feature.io.results.FeatureOutputMetadata;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.feature.io.results.LabelledResultsCollector;
import org.anchoranalysis.feature.io.results.LabelledResultsVector;
import org.anchoranalysis.feature.io.results.group.GroupedResults;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.InputOutputContextSubdirectoryCache;

/**
 * Outputs feature calculation results in different ways.
 *
 * <p>The outputs are (based upon the default output-names in {@link FeatureOutputNames}):
 *
 * <pre>
 * Two CSV files outputted:
 *    features.csv:              all the features in a single CSV file
 *    featuresAggregated.csv:    aggregate-functions applied to each group of features (based upon their group identifier)
 *
 * Additionally, the following files might be outputted for each group
 *    featuresGroup.csv:            the features for a particular-group
 *    featuresAggregatedGroup.xml   the aggregate-functions applied to this particular-group (in an XML format)
 * </pre>
 *
 * <p>Where possible, features.csv are written directly as they are encountered (eager-mode). But
 * with certain settings, all features must be calculated before decisions can be made on what to
 * output (lazy-mode).
 *
 * @author Owen Feehan
 */
class WriteWithGroups implements LabelledResultsCollector {

    /** Where to write the non-aggregated results. */
    private final LabelledResultsCSVWriter nonAggregatedResults;

    /** Where to store results for later aggregation. */
    private final GroupedResults groupedResults;

    /**
     * Creates with appropriate support classes for outputting.
     *
     * @param outputMetadata metadata needed for determining output-names and CSV headers.
     * @param writerCreator when true, columns containing all {@link Double#NaN} values are removed
     *     before outputting.
     * @param removeNaNColumns when true, columns containing all {@link Double#NaN} values are
     *     removed before outputting.
     * @throws OutputWriteFailedException if a CSV for (non-aggregated) features fails to be
     *     created.
     */
    public WriteWithGroups(
            FeatureCSVMetadataForOutput outputMetadata,
            FeatureCSVWriterFactory writerCreator,
            boolean removeNaNColumns)
            throws OutputWriteFailedException {
        groupedResults = new GroupedResults();
        nonAggregatedResults =
                LabelledResultsCSVWriterFactory.create(
                        outputMetadata,
                        writerCreator,
                        Optional.of(groupedResults::addResultsFor),
                        removeNaNColumns);
        nonAggregatedResults.start();
    }

    @Override
    public void add(LabelledResultsVector results) {
        nonAggregatedResults.add(results);
    }

    @Override
    public void flushAndClose(
            Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
            boolean includeGroups,
            Function<InputOutputContext, FeatureCSVWriterFactory> csvWriterCreator,
            FeatureOutputMetadata outputMetadata,
            InputOutputContext context)
            throws OutputWriteFailedException {

        nonAggregatedResults.end();

        InputOutputContextSubdirectoryCache contextGroups =
                new InputOutputContextSubdirectoryCache(
                        context.subdirectory("grouped", true), true);

        groupedResults.writeGroupResults(
                featuresAggregate,
                includeGroups,
                outputMetadata,
                metadata -> csvWriterCreator.apply(context).create(metadata),
                contextGroups);
    }
}
