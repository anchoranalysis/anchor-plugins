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

import java.io.IOException;
import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.BeanXMLLoader;
import org.anchoranalysis.bean.xml.exception.BeanXMLException;
import org.anchoranalysis.bean.xml.factory.BeanPathCalculator;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifierConstant;
import org.anchoranalysis.experiment.bean.io.InputOutputExperiment;
import org.anchoranalysis.experiment.bean.log.ToConsole;
import org.anchoranalysis.experiment.bean.processor.SequentialProcessor;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.files.SearchDirectory;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.bean.rules.IgnoreUnderscorePrefixUnless;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.file.path.prefixer.DirectoryStructure;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFiles;
import org.anchoranalysis.plugin.io.bean.input.stack.Stacks;

/**
 * A quick way of defining an {@link InputOutputExperiment} where several assumptions are made.
 *
 * @author Owen Feehan
 * @param <S> shared-state
 */
public class QuickExperiment<S> extends Experiment {

    // START BEAN PROPERTIES
    /**
     * A string indicating the input file(s)
     *
     * <p>Either: 1. a file-path to a single image 2. a file glob matching several images (e.g.
     * /somedir/somefile*.png) 3. a file-path ending in .xml or .XML. This is then interpreted
     * treated a a paths to BeanXML describing a NamedMultiCollectionInputManager
     */
    @BeanField @Getter @Setter private String fileInput;

    @BeanField @Getter @Setter private String directoryOutput;

    @BeanField @Getter @Setter private Task<MultiInput, S> task;

    @BeanField @Getter @Setter private String inputName = "stackInput";

    @BeanField @Getter @Setter
    private OutputWriteSettings outputWriteSettings = new OutputWriteSettings();
    // END BEAN PROPERTIES

    // Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks
    // elsewhere
    private BeanInstanceMap defaultInstances;

    private InputOutputExperiment<MultiInput, S> delegate;

    private ExperimentIdentifierConstant experimentIdentifier =
            new ExperimentIdentifierConstant("single", "1.0");

    public QuickExperiment() {
        delegate = new InputOutputExperiment<>();
        delegate.setExperimentIdentifier(experimentIdentifier);
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    public void executeExperiment(ExecutionArguments arguments)
            throws ExperimentExecutionException {
        delegate.associateXML(getXMLConfiguration());

        Path combinedFileFilter = BeanPathCalculator.pathFromBean(this, fileInput);

        if (NonImageFileFormat.XML.matches(combinedFileFilter)) {
            createFromXMLBean(combinedFileFilter);
        } else {
            createFromFile(combinedFileFilter, arguments);
        }

        // Log Reporter
        delegate.setLogExperiment(new ToConsole());
        delegate.setTaskProcessor(createTask());

        try {
            delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new ExperimentExecutionException(e);
        }

        delegate.executeExperiment(arguments);
    }

    @Override
    public boolean useDetailedLogging() {
        return delegate.useDetailedLogging();
    }

    private void createFromXMLBean(Path combinedFileFilter) throws ExperimentExecutionException {
        // Creates from an XML bean
        delegate.setInput(createInputManagerBean(combinedFileFilter));

        Path outBasePath = combinedFileFilter.getParent();
        delegate.setOutput(createOutputManager(outBasePath));
    }

    private void createFromFile(Path combinedFileFilter, ExecutionArguments arguments)
            throws ExperimentExecutionException {
        // Creates from a file
        SearchDirectory search = new SearchDirectory();
        search.setFileFilterAndDirectory(combinedFileFilter);

        delegate.setInput(createInputManagerImageFile(search));

        try {
            delegate.setOutput(
                    createOutputManager(
                            search.getDirectoryAsPathEnsureAbsolute(
                                    arguments.createInputContext())));

        } catch (IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    private SequentialProcessor<MultiInput, S> createTask() {
        SequentialProcessor<MultiInput, S> taskProcessor = new SequentialProcessor<>();
        taskProcessor.setTask(task);
        return taskProcessor;
    }

    private InputManager<MultiInput> createInputManagerBean(Path beanPath)
            throws ExperimentExecutionException {
        try {
            return BeanXMLLoader.loadBean(beanPath);
        } catch (BeanXMLException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    private InputManager<MultiInput> createInputManagerImageFile(SearchDirectory fs) {
        return new MultiInputManager(inputName, new Stacks(new NamedFiles(fs)));
    }

    private OutputManager createOutputManager(Path baseDirectory) {

        OutputManager manager = new OutputManager();
        manager.setSilentlyDeleteExisting(true);
        manager.setOutputsEnabled(new IgnoreUnderscorePrefixUnless());

        try {
            manager.localise(getLocalPath());
        } catch (BeanMisconfiguredException e) {
            // Should never arise, as getLocalPath() should always be absolute
            throw new AnchorFriendlyRuntimeException(e);
        }

        manager.setPrefixer(createFilePathResolver(baseDirectory));
        manager.setOutputWriteSettings(outputWriteSettings);
        return manager;
    }

    private DirectoryStructure createFilePathResolver(Path baseDirectory) {

        Path pathDirectoryOut = BeanPathCalculator.pathFromBean(this, directoryOutput);

        DirectoryStructure resolver = new DirectoryStructure();
        resolver.setPrefixToRemove(baseDirectory.toString());
        resolver.setPrefix(pathDirectoryOut.toString());
        try {
            resolver.localise(getLocalPath());
        } catch (BeanMisconfiguredException e) {
            // Should never arise, as getLocalPath() should always be absolute
            throw new AnchorFriendlyRuntimeException(e);
        }
        return resolver;
    }
}
