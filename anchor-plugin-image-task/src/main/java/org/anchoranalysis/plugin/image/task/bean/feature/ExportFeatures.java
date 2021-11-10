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

package org.anchoranalysis.plugin.image.task.bean.feature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;

/**
 * Calculates features and exports them as a CSV file.
 *
 * <p>Aggregated-features (based upon a certain grouping) can also be calculated.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>features</td><td>yes</td><td>a single CSV file of feature-calculations where each row is an object.</td></tr>
 * <tr><td>featuresAggregated</td><td>yes</td><td>a single CSV file of feature-calculations where each row is a group (with aggregated features of the objects within).</td></tr>
 * <tr><td>featuresGroup</td><td>no</td><td>a CSV file of feature-calculations per group, where each row is an object.</td></tr>
 * <tr><td>featuresAggregatedGroup</td><td>no</td><td>a XML file of aggregated feature-calculations per group</td></tr>
 * <tr><td>thumbnails</td><td>yes</td><td>a small picture for each row in the {@code features} CSV illustrating what the row represents. How this picture is formulated depends on the {@code source}.</td></tr>
 * <tr><td rowspan="3"><i>outputs from a sub-class of {@link FeatureSource} as used in {@code source}.</i></td></tr>
 * <tr><td rowspan="3"><i>outputs from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <T> See {@link Task}
 * @param <S> a source-of-features that is duplicated for each new thread (to prevent any
 *     concurrency issues)
 * @param <U> feature-input type for {@code features} bean-field
 */
public class ExportFeatures<T extends InputFromManager, S, U extends FeatureInput>
        extends Task<T, FeatureExporter<S>> {

    private static final NamedFeatureStoreFactory STORE_FACTORY_AGGREGATE =
            NamedFeatureStoreFactory.bothNameAndParams();

    public static final FeatureOutputNames OUTPUT_RESULTS = new FeatureOutputNames();

    // START BEAN PROPERTIES
    /** Source of feature-values to be exported. */
    @BeanField @Getter @Setter private FeatureSource<T, S, U> source;

    /**
     * If non-null, this file-path is used to determine the group of the file.
     *
     * <p>If null, no group is included.
     */
    @BeanField @OptionalBean @Getter @Setter private DerivePath group;

    /** Translates an input file name to a unique ID. */
    @BeanField @OptionalBean @Getter @Setter private DerivePath id;

    /** The features to be exported (after possibly some manipulation or augmentation). */
    @BeanField @NonEmpty @Getter @Setter
    private List<NamedBean<FeatureListProvider<U>>> features = new ArrayList<>();

    /**
     * Features applied to each group to aggregate values (accepting {@link FeatureInputResults}).
     */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputResults>>> featuresAggregate;

    /** Visual style for how feature export occurs. */
    @BeanField @Getter @Setter ExportFeaturesStyle style = new ExportFeaturesStyle();
    // END BEAN PROPERTIES

    @Override
    public FeatureExporter<S> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<T> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            FeatureExporterContext context = style.deriveContext(params.getContext());
            return source.createExporter(
                    source.headers(isGroupGeneratorDefined()), features, OUTPUT_RESULTS, context);
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInput(InputBound<T, FeatureExporter<S>> input) throws JobExecutionException {
        try {
            Optional<String> groupName =
                    extractGroupNameFromGenerator(
                            input.getInput().pathForBindingRequired(),
                            input.getContextJob().isDebugEnabled());

            FeatureCalculationContext<S> calculationContext =
                    input.getSharedState()
                            .createCalculationContext(
                                    groupName,
                                    input.getContextExperiment().getExecutionTimeRecorder(),
                                    input.getContextJob());

            // Process input to calculate and output features.
            source.calculateAndOutput(input.getInput(), calculationContext);

        } catch (OperationFailedException | InputReadFailedException | DerivePathException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(FeatureExporter<S> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // Write the aggregate features
        try {
            sharedState.writeGroupedResults(
                    featuresAggregateAsStore(),
                    source.includeGroupInExperiment(isGroupGeneratorDefined()),
                    contextForWriter -> style.deriveContext(contextForWriter)::csvWriter,
                    context);
            sharedState.closeAnyOpenIO();
        } catch (OutputWriteFailedException | ProvisionFailedException | IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs()
                .addEnabledOutputFirst(
                        FeatureExporter.OUTPUT_THUMBNAILS,
                        OUTPUT_RESULTS.getCsvFeaturesNonAggregated(),
                        OUTPUT_RESULTS.getCsvFeaturesAggregated().get()); // NOSONAR
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return source.inputTypesExpected();
    }

    private boolean isGroupGeneratorDefined() {
        return group != null;
    }

    private Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregateAsStore()
            throws ProvisionFailedException {
        return OptionalUtilities.map(
                Optional.ofNullable(featuresAggregate),
                STORE_FACTORY_AGGREGATE::createNamedFeatureList);
    }

    /** Determines the group name corresponding to an {@code inputPath} and the group-generator. */
    private Optional<String> extractGroupNameFromGenerator(Path inputPath, boolean debugMode)
            throws DerivePathException {
        return filePathAsIdentifier(Optional.ofNullable(group), inputPath, debugMode);
    }

    private static Optional<String> filePathAsIdentifier(
            Optional<DerivePath> generator, Path path, boolean debugMode)
            throws DerivePathException {
        return OptionalUtilities.map(
                generator,
                gen ->
                        FilePathToUnixStyleConverter.toStringUnixStyle(
                                gen.deriveFrom(path, debugMode)));
    }
}
