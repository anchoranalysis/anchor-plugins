/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.path;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.bean.root.RootPathMap;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.text.StringGenerator;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class RecordFilepathsTask<T extends InputFromManager> extends Task<T, StringBuilder> {

    // START BEAN PROPERTIES

    // The name of the RootPath to associate with this fileset. If empty, it is ignored.
    @BeanField @AllowEmpty private String rootName = "";
    // END BEAN PROPERTIES

    @Override
    public StringBuilder beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        return new StringBuilder();
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(InputFromManager.class);
    }

    @Override
    public void doJobOnInputObject(InputBound<T, StringBuilder> params)
            throws JobExecutionException {

        Path path;
        try {
            path = params.getInputObject().pathForBindingRequired();
        } catch (AnchorIOException e) {
            throw new JobExecutionException(e);
        }

        StringBuilder sb = params.getSharedState();

        if (!rootName.isEmpty()) {
            try {
                path =
                        RootPathMap.instance()
                                .split(path, rootName, params.context().isDebugEnabled())
                                .getPath();
            } catch (AnchorIOException e) {
                throw new JobExecutionException(e);
            }
        }
        sb.append("<item>");
        sb.append(path);
        sb.append("</item>");
        sb.append('\n');
    }

    @Override
    public void afterAllJobsAreExecuted(StringBuilder sharedState, BoundIOContext context)
            throws ExperimentExecutionException {

        context.getOutputManager()
                .getWriterAlwaysAllowed()
                .write("list", () -> new StringGenerator(sharedState.toString()));
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return true;
    }
}
