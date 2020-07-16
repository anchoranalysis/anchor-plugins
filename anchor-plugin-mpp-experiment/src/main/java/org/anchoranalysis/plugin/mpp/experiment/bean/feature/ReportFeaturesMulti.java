/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.mpp.io.bean.report.feature.ReportFeatureForSharedObjects;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

public class ReportFeaturesMulti extends Task<MultiInput, CSVWriter> {

    // START BEAN PROPERTIES
    @BeanField private List<ReportFeatureForSharedObjects> listReportFeatures = new ArrayList<>();

    @BeanField @OptionalBean private DefineOutputterMPP define;
    // END BEAN PROPERTIES

    @Override
    public CSVWriter beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {

        Optional<CSVWriter> writer;
        try {
            writer =
                    CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());
        } catch (AnchorIOException e) {
            throw new ExperimentExecutionException(e);
        }

        if (!writer.isPresent()) {
            throw new ExperimentExecutionException(
                    "'featureReport' output not enabled, as is required");
        }

        List<String> headerNames = ReportFeatureUtilities.genHeaderNames(listReportFeatures, null);

        headerNames.add(0, "id");
        writer.get().writeHeaders(headerNames);

        return writer.get();
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public void doJobOnInputObject(InputBound<MultiInput, CSVWriter> input)
            throws JobExecutionException {

        CSVWriter writer = input.getSharedState();

        if (!writer.isOutputEnabled()) {
            return;
        }

        try {
            define.processInputMPP(
                    input.getInputObject(),
                    input.context(),
                    soMPP ->
                            writeFeaturesIntoReporter(
                                    soMPP,
                                    writer,
                                    input.getInputObject().descriptiveName(),
                                    input.getLogger()));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private void writeFeaturesIntoReporter(
            MPPInitParams soMPP, CSVWriter writer, String inputDescriptiveName, Logger logger) {
        List<TypedValue> rowElements =
                ReportFeatureUtilities.genElementList(listReportFeatures, soMPP, logger);

        rowElements.add(0, new TypedValue(inputDescriptiveName, false));

        writer.writeRow(rowElements);
    }

    @Override
    public void afterAllJobsAreExecuted(CSVWriter writer, BoundIOContext context)
            throws ExperimentExecutionException {
        writer.close();
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    public List<ReportFeatureForSharedObjects> getListReportFeatures() {
        return listReportFeatures;
    }

    public void setListReportFeatures(List<ReportFeatureForSharedObjects> listReportFeatures) {
        this.listReportFeatures = listReportFeatures;
    }

    public DefineOutputterMPP getDefine() {
        return define;
    }

    public void setDefine(DefineOutputterMPP define) {
        this.define = define;
    }
}
