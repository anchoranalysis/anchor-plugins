/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.experiment;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.error.BeanStrangeException;
import org.anchoranalysis.bean.xml.BeanXmlLoader;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.bean.xml.factory.BeanPathUtilities;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.plugin.io.bean.input.filter.FilterIfDebug;
import org.apache.commons.configuration.XMLConfiguration;

/** Helper class for QuickMultiDatasetExperiment */
class RepeatedExperimentFromXml<T extends InputFromManager, S> {

    private RepeatedExperiment<T, S> delegate;

    private String beanExtension;

    /** Resolved path to the folder containing datasets */
    private Path pathDatasetFolder;

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
        pathDatasetFolder = getCombinedPath(folderDataset);

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
            ExperimentExecutionArguments expArgs,
            BeanInstanceMap defaultInstances)
            throws ExperimentExecutionException {

        // Create a bean for the dataset and execute
        InputManager<T> input = createInputManager(datasetName, pathDatasetFolder, beanExtension);

        delegate.executeForManager(input, expArgs, defaultInstances);
    }

    public void executeForManager(
            InputManager<T> inputManager,
            ExperimentExecutionArguments expArgs,
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
            String datasetName, Path pathFolder, String beanExtension)
            throws ExperimentExecutionException {
        InputManager<T> input = loadInputManagerFromXml(datasetName, pathFolder, beanExtension);
        return filterIfDebug(input);
    }

    private InputManager<T> filterIfDebug(InputManager<T> input) {
        FilterIfDebug<T> filter = new FilterIfDebug<>();
        filter.setInput(input);
        return filter;
    }

    /*** Loads a bean from the file-system */
    private InputManager<T> loadInputManagerFromXml(
            String datasetName, Path pathFolder, String beanExtension)
            throws ExperimentExecutionException {

        Path pathDataset = pathFolder.resolve(datasetName.concat(beanExtension));
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
