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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.bean.init.MarksInitialization;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputterMarks;

/**
 * Creates a report of feature values from a {@link DefineOutputterMarks} and a {@link MultiInput}.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td rowspan="3"><i>inherited from {@link ExportReportFeatures}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class ExportReportFeaturesFromMulti
        extends ExportReportFeatures<MultiInput, CSVWriter, MarksInitialization> {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private DefineOutputterMarks define;
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<MultiInput> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        Optional<CSVWriter> writer;
        try {
            writer = createWriter(params.getOutputter().getChecked());
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
            define.processInputMPP(
                    input.getInput(),
                    input.createInitializationContext(),
                    initialization ->
                            writeFeaturesIntoReporter(
                                    initialization,
                                    writer,
                                    Optional.of(input.getInput().name()),
                                    input.getLogger()));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(CSVWriter writer, InputOutputContext context)
            throws ExperimentExecutionException {
        writer.close();
    }
}
