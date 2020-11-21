/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.structure;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.ReplaceInputManager;
import org.anchoranalysis.experiment.io.ReplaceOutputManager;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.plugin.quick.bean.experiment.QuickMultiDatasetExperiment;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * Similar to {@link QuickMultiDatasetStructuredExperiment} put sets properties according to an
 * expected directory structure.
 *
 * <p>
 *
 * <pre>
 *   Project Directory/
 *      Experiments/			# where Experiments are stored
 *      Experiments/include/	# where a loggerExperiment.xml and loggerTask.xml can be found
 *      Filesets/				# where Filesets are stored
 *      IO/OutputManager/		# where OutputManagers are stored
 * </pre>
 *
 * <p>It also assumes a certain naming structure for the xml files in each, based upon an
 * experiment-type parameter $1.
 *
 * <pre>
 * 	Filesets/For$1/		# the first letter of $1 is capitalized)
 * 	IO/OutputManager/$1.xml
 * </pre>
 *
 * <p>his allows many parameters of {@link QuickMultiDatasetStructuredExperiment} to be easily set.
 */
public class QuickMultiDatasetStructuredExperiment<T extends InputFromManager, S> extends Experiment
        implements ReplaceInputManager, ReplaceOutputManager {

    // START BEAN PROPERTIES
    /**
     * How many directories to recurse back from the current file to the Experiments/ directory
     *
     * <p>0 implies that the file is directly in Experiments/ 1 implies that the file is in
     * Experiments/SOMESUBDIR/ etc.
     */
    @BeanField @Getter @Setter private int directoryDistance = 0;

    /**
     * The value of $1 (see comment above) in naming-structure. It should be in camel-case
     * typically.
     */
    @BeanField @Getter @Setter private String experimentType;
    // END BEAN PROPERTIES

    private QuickMultiDatasetExperiment<T, S> delegate;

    // Have we populated delegate yet?
    private boolean populatedDelegate = false;

    public QuickMultiDatasetStructuredExperiment() {
        delegate = new QuickMultiDatasetExperiment<>();
    }

    @Override
    public void localise(Path path) throws BeanMisconfiguredException {
        delegate.localise(path);
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        populateDelegateIfNeeded();
        delegate.checkMisconfigured(defaultInstances);
    }

    private void populateDelegateIfNeeded() {
        if (!populatedDelegate) {
            delegate.setDirectoryDataset(directoryDataset());
            delegate.setOutput(output());
            delegate.setLogExperimentPath(loggerPath("Experiment"));
            delegate.setLogTaskPath(loggerPath("Task"));

            populatedDelegate = true;
        }
    }

    private String directoryDataset() {
        return String.format(
                "%s../Filesets/For%s/", pathPrefix(), StringUtils.capitalize(experimentType));
    }

    private String output() {
        String directory = pathPrefix() + "../IO/OutputManager";
        return NonImageFileFormat.XML.buildPath(directory, experimentType);
    }

    private String loggerPath(String suffix) {
        String directory = pathPrefix() + "include/";
        String filename = "logger" + StringUtils.capitalize(suffix);
        return NonImageFileFormat.XML.buildPath(directory, filename);
    }

    private String pathPrefix() {
        if (directoryDistance == 0) {
            return "./";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < directoryDistance; i++) {
                sb.append("../");
            }
            return sb.toString();
        }
    }

    @Override
    public void associateXml(XMLConfiguration xmlConfiguration) {
        super.associateXml(xmlConfiguration);
        delegate.associateXml(xmlConfiguration);
    }

    @Override
    public void replaceOutputManager(OutputManager outputter) throws OperationFailedException {
        populateDelegateIfNeeded();
        delegate.replaceOutputManager(outputter);
    }

    @Override
    public void replaceInputManager(InputManager<?> inputManager) throws OperationFailedException {
        populateDelegateIfNeeded();
        delegate.replaceInputManager(inputManager);
    }

    @Override
    public void executeExperiment(ExperimentExecutionArguments arguments)
            throws ExperimentExecutionException {
        populateDelegateIfNeeded();
        delegate.executeExperiment(arguments);
    }

    @Override
    public boolean useDetailedLogging() {
        return delegate.useDetailedLogging();
    }

    public String getDatasetSpecific() {
        return delegate.getDatasetSpecific();
    }

    public void setDatasetSpecific(String datasetSpecific) {
        delegate.setDatasetSpecific(datasetSpecific);
    }

    public String getBeanExtension() {
        return delegate.getBeanExtension();
    }

    public void setBeanExtension(String beanExtension) {
        delegate.setBeanExtension(beanExtension);
    }

    public String getIdentifierSuffix() {
        return delegate.getIdentifierSuffix();
    }

    public void setIdentifierSuffix(String identifierSuffix) {
        delegate.setIdentifierSuffix(identifierSuffix);
    }

    public StringSet getDatasets() {
        return delegate.getDatasets();
    }

    public void setDatasets(StringSet datasets) {
        delegate.setDatasets(datasets);
    }

    public int getMaxNumberProcessors() {
        return delegate.getMaxNumberProcessors();
    }

    public void setMaxNumberProcessors(int maxNumProcessors) {
        delegate.setMaxNumberProcessors(maxNumProcessors);
    }

    public boolean isSuppressExceptions() {
        return delegate.isSuppressExceptions();
    }

    public void setSuppressExceptions(boolean suppressExceptions) {
        delegate.setSuppressExceptions(suppressExceptions);
    }

    public Task<T, S> getTask() {
        return delegate.getTask();
    }

    public void setTask(Task<T, S> task) {
        delegate.setTask(task);
    }
}
