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
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.io.bean.report.feature.ReportFeatureForSharedObjects;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputterMPP;

public class ExportReportFeatures extends Task<MultiInput, CSVWriter> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private List<ReportFeatureForSharedObjects> listReportFeatures = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private DefineOutputterMPP define;
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            Outputter outputter, ConcurrencyPlan concurrencyPlan, ParametersExperiment params)
            throws ExperimentExecutionException {

        Optional<CSVWriter> writer;
        try {
            writer = CSVWriter.createFromOutputter("featureReport", outputter.getChecked());
        } catch (AnchorIOException e) {
            throw new ExperimentExecutionException(e);
        }

        if (!writer.isPresent()) {
            throw new ExperimentExecutionException(
                    "'featureReport' output not enabled, as is required");
        }

        List<String> headerNames = ReportFeatureUtilities.headerNames(listReportFeatures, null);

        headerNames.add(0, "id");
        writer.get().writeHeaders(headerNames);

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
                    input.context(),
                    soMPP ->
                            writeFeaturesIntoReporter(
                                    soMPP,
                                    writer,
                                    input.getInput().descriptiveName(),
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

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }

    private void writeFeaturesIntoReporter(
            MPPInitParams soMPP, CSVWriter writer, String inputDescriptiveName, Logger logger) {
        List<TypedValue> rowElements =
                ReportFeatureUtilities.elementList(listReportFeatures, soMPP, logger);

        rowElements.add(0, new TypedValue(inputDescriptiveName, false));

        writer.writeRow(rowElements);
    }
}
