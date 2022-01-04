/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.report.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.bean.ReportFeature;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputter;

/**
 * Creates a report of feature values from a {@link DefineOutputter} and a {@link MultiInput}.
 *
 * <p>It uses {@code ReportFeature<ImageInitialization>} features.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value ExportReportFeatures#OUTPUT_REPORT}</td><td>yes</td><td>A CSV file with the report-features as columns (and an ID column), one for each input.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link ExportReportFeatures}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class ExportReportFeatures extends Task<MultiInput, CSVWriter> {

    private static final String OUTPUT_REPORT = "featureReport";

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private DefineOutputter define;

    @BeanField @Getter @Setter
    private List<ReportFeature<ImageInitialization>> listReportFeatures = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<MultiInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {

        Optional<CSVWriter> writer;
        try {
            writer = createWriter(parameters.getOutputter().getChecked());
        } catch (OutputWriteFailedException e) {
            throw new ExperimentExecutionException(e);
        }

        if (!writer.isPresent()) {
            throw new ExperimentExecutionException(
                    "'featureReport' output not enabled, as is required");
        }

        writer.get().writeHeaders(headerNames(Optional.of("id")));

        return writer.get();
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public void doJobOnInput(InputBound<MultiInput, CSVWriter> input) throws JobExecutionException {

        CSVWriter writer = input.getSharedState();

        if (!writer.isOutputEnabled()) {
            return;
        }

        try {
            define.process(
                    input,
                    initialization ->
                            writeFeaturesIntoReporter(
                                    initialization,
                                    writer,
                                    Optional.of(input.getInput().identifier()),
                                    input.getContextJob().operationContext()));
        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(CSVWriter writer, InputOutputContext context)
            throws ExperimentExecutionException {
        writer.close();
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_REPORT);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private List<String> headerNames(Optional<String> prefixColumnName) {
        List<String> headerNames = ReportFeatureUtilities.headerNames(listReportFeatures);
        prefixColumnName.ifPresent(headerNames::add);
        return headerNames;
    }

    private void writeFeaturesIntoReporter(
            ImageInitialization featureParam,
            CSVWriter writer,
            Optional<String> prefixValue,
            OperationContext context) {
        List<TypedValue> rowElements =
                ReportFeatureUtilities.elementList(listReportFeatures, featureParam, context);

        if (prefixValue.isPresent()) {
            rowElements.add(0, new TypedValue(prefixValue.get(), false));
        }

        writer.writeRow(rowElements);
    }

    private Optional<CSVWriter> createWriter(OutputterChecked outputter)
            throws OutputWriteFailedException {
        return CSVWriter.createFromOutputter(OUTPUT_REPORT, outputter);
    }
}
