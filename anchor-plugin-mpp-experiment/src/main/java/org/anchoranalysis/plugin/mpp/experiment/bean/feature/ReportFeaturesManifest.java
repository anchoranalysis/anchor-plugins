/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.plugin.io.manifest.CoupledManifests;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;

public class ReportFeaturesManifest extends TaskWithoutSharedState<ManifestCouplingDefinition> {

    // START BEAN PROPERTIES
    @BeanField
    private List<ReportFeature<ManifestRecorderFile>> listReportFeatures = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ManifestCouplingDefinition.class);
    }

    @Override
    public void doJobOnInputObject(InputBound<ManifestCouplingDefinition, NoSharedState> params)
            throws JobExecutionException {

        Logger logger = params.getLogger();
        ManifestCouplingDefinition input = params.getInputObject();
        BoundOutputManagerRouteErrors outputManager = params.getOutputManager();

        Optional<CSVWriter> writer;
        try {
            writer =
                    CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());
        } catch (AnchorIOException e1) {
            throw new JobExecutionException(e1);
        }

        try {

            if (!writer.isPresent()) {
                return;
            }

            writer.get()
                    .writeHeaders(
                            ReportFeatureUtilities.genHeaderNames(listReportFeatures, logger));

            Iterator<CoupledManifests> itr = input.iteratorCoupledManifests();
            while (itr.hasNext()) {

                CoupledManifests mr = itr.next();

                List<TypedValue> rowElements =
                        ReportFeatureUtilities.genElementList(
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

    public List<ReportFeature<ManifestRecorderFile>> getListReportFeatures() {
        return listReportFeatures;
    }

    public void setListReportFeatures(
            List<ReportFeature<ManifestRecorderFile>> listReportFeatures) {
        this.listReportFeatures = listReportFeatures;
    }
}
