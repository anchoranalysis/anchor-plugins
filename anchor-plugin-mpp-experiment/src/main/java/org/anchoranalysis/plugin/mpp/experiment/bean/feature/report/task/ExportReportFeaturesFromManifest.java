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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.io.bean.report.feature.ReportFeature;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.io.manifest.CoupledManifests;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;

public class ExportReportFeaturesFromManifest
        extends TaskWithoutSharedState<ManifestCouplingDefinition> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private List<ReportFeature<ManifestRecorderFile>> listReportFeatures = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ManifestCouplingDefinition.class);
    }

    @Override
    public void doJobOnInput(InputBound<ManifestCouplingDefinition, NoSharedState> params)
            throws JobExecutionException {

        Logger logger = params.getLogger();
        ManifestCouplingDefinition input = params.getInput();
        Outputter outputter = params.getOutputter();

        Optional<CSVWriter> writer;
        try {
            writer = CSVWriter.createFromOutputter("featureReport", outputter.getChecked());
        } catch (AnchorIOException e1) {
            throw new JobExecutionException(e1);
        }

        try {

            if (!writer.isPresent()) {
                return;
            }

            writer.get()
                    .writeHeaders(ReportFeatureUtilities.headerNames(listReportFeatures, logger));

            Iterator<CoupledManifests> itr = input.iteratorCoupledManifests();
            while (itr.hasNext()) {

                CoupledManifests mr = itr.next();

                List<TypedValue> rowElements =
                        ReportFeatureUtilities.elementList(
                                listReportFeatures, mr.getFileManifest(), logger);

                try {
                    writer.get().writeRow(rowElements);
                } catch (NumberFormatException e) {
                    throw new JobExecutionException(e);
                }
            }

        } finally {
            writer.get().close();
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
