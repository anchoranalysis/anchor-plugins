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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.io.manifest.CoupledManifests;
import org.anchoranalysis.plugin.io.manifest.DeserializedManifest;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;


/**
 * Creates a report of feature values from a {@link DeserializedManifest} and a {@link ManifestCouplingDefinition}.
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
public class ExportReportFeaturesFromManifest
        extends ExportReportFeatures<ManifestCouplingDefinition,NoSharedState,DeserializedManifest> {

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ManifestCouplingDefinition.class);
    }

    @Override
    public NoSharedState beforeAnyJobIsExecuted(Outputter outputter,
            ConcurrencyPlan concurrencyPlan, List<ManifestCouplingDefinition> inputs,
            ParametersExperiment params) throws ExperimentExecutionException {
        return NoSharedState.INSTANCE;
    }
    
    @Override
    public void doJobOnInput(InputBound<ManifestCouplingDefinition, NoSharedState> params)
            throws JobExecutionException {

        try {
            Optional<CSVWriter> writer = createWriter(params.getOutputter().getChecked());
            if (writer.isPresent()) {
                writeCSV(writer.get(), params.getInput(), params.getLogger());
            }

        } catch (OutputWriteFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(NoSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }
    
    private void writeCSV(CSVWriter writer, ManifestCouplingDefinition input, Logger logger)
            throws JobExecutionException {
        try {
            writer.writeHeaders( headerNames(Optional.empty()) );

            Iterator<CoupledManifests> iterator = input.iteratorCoupledManifests();
            while (iterator.hasNext()) {
                try {
                    writeFeaturesIntoReporter(iterator.next().getJobManifest(), writer, Optional.empty(), logger);
                } catch (NumberFormatException e) {
                    throw new JobExecutionException(e);
                }
            }

        } finally {
            writer.close();
        }
    }
}
