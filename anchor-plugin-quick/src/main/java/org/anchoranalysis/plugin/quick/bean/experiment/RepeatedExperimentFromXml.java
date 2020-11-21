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

package org.anchoranalysis.plugin.quick.bean.experiment;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.exception.BeanStrangeException;
import org.anchoranalysis.bean.xml.BeanXmlLoader;
import org.anchoranalysis.bean.xml.exception.BeanXmlException;
import org.anchoranalysis.bean.xml.factory.BeanPathUtilities;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.plugin.io.bean.input.filter.FilterIfDebug;
import org.apache.commons.configuration.XMLConfiguration;

/** Helper class for QuickMultiDatasetExperiment */
class RepeatedExperimentFromXml<T extends InputFromManager, S> {

    private RepeatedExperiment<T, S> delegate;

    private String beanExtension;

    /** Resolved path to the folder containing datasets */
    private Path pathDatasetDirectory;

    /** Path to bean this helper is used from */
    private Path beanLocalPath;

    public RepeatedExperimentFromXml(ExperimentIdentifier experimentIdentifier) {
        delegate = new RepeatedExperiment<>(experimentIdentifier);
    }

    /** First method called ONCE after the constructor */
    public void firstLocalise(
            Path beanLocalPath, String loggerPathExperiment, String loggerPathTask, String output) {

        this.beanLocalPath = beanLocalPath;

        // We create these other beans, before we check the configuration. This is a bit hacky
        delegate.setLogExperiment(extractLogReporterBean(loggerPathExperiment));
        delegate.setLogTask(extractLogReporterBean(loggerPathTask));

        delegate.setOutput(extractBean(output, "OutputManager"));
    }

    /** Second method called ONCE before calling any executeForManager* methods */
    public void secondInitBeforeExecution(
            XMLConfiguration xmlConfiguration,
            String folderDataset,
            String beanExtension,
            JobProcessor<T, S> taskProcessor) {
        delegate.init(xmlConfiguration, taskProcessor);

        // The folder where the datasets are stored
        pathDatasetDirectory = getCombinedPath(folderDataset);

        this.beanExtension = beanExtension;
    }

    /**
     * @param datasetName the name of the dataset to execute
     * @param expArgs
     * @param defaultInstances
     * @throws ExperimentExecutionException
     */
    public void executeForManagerFromXml(
            String datasetName,
            ExecutionArguments expArgs,
            BeanInstanceMap defaultInstances)
            throws ExperimentExecutionException {

        // Create a bean for the dataset and execute
        InputManager<T> input = createInputManager(datasetName, pathDatasetDirectory, beanExtension);

        delegate.executeForManager(input, expArgs, defaultInstances);
    }

    public void executeForManager(
            InputManager<T> inputManager,
            ExecutionArguments expArgs,
            BeanInstanceMap defaultInstances)
            throws ExperimentExecutionException {
        delegate.executeForManager(inputManager, expArgs, defaultInstances);
    }

    public void setOutput(OutputManager output) {
        delegate.setOutput(output);
    }

    public LoggingDestination extractLogReporterBean(String relativePath) {
        return extractBean(relativePath, "LogReporterBean");
    }

    private <U> U extractBean(String relativePath, String friendlyName) {
        Path path = getCombinedPath(relativePath);
        try {
            U bean = BeanXmlLoader.loadBean(path); // NOSONAR
            return bean;
        } catch (BeanXmlException e) {
            throw new BeanStrangeException(
                    String.format(
                            "Cannot create %s in QuickMultiDatasetExperiment (opening \"%s\")",
                            friendlyName, path),
                    e);
        }
    }

    /*** Creates an input-manager for a dataset located in BeanXML in a particular folder. Maybe adds a filter in debug-mode */
    private InputManager<T> createInputManager(
            String datasetName, Path pathDirectory, String beanExtension)
            throws ExperimentExecutionException {
        InputManager<T> input = loadInputManagerFromXml(datasetName, pathDirectory, beanExtension);
        return filterIfDebug(input);
    }

    private InputManager<T> filterIfDebug(InputManager<T> input) {
        FilterIfDebug<T> filter = new FilterIfDebug<>();
        filter.setInput(input);
        return filter;
    }

    /*** Loads a bean from the filesystem */
    private InputManager<T> loadInputManagerFromXml(
            String datasetName, Path pathDirectory, String beanExtension)
            throws ExperimentExecutionException {

        Path pathDataset = pathDirectory.resolve(datasetName.concat(beanExtension));
        try {
            return BeanXmlLoader.loadBean(pathDataset);
        } catch (BeanXmlException e) {
            throw new ExperimentExecutionException(
                    String.format("Cannot create bean for dataset '%s'", pathDataset), e);
        }
    }

    private Path getCombinedPath(String relativePath) {
        assert (beanLocalPath != null);
        return BeanPathUtilities.combine(beanLocalPath, Paths.get(relativePath));
    }
}
