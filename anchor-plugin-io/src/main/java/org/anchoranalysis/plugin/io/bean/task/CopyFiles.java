/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.task;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.files.FileWithDirectoryInput;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.math.arithmetic.Counter;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.CopyFilesMethod;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.Bytewise;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.CopyFilesNaming;
import org.anchoranalysis.plugin.io.shared.RecordingCounter;

/**
 * Copy files to the output-directory, possibly changing the name or performing other operations like compression in the process.
 * 
 * <p>Unusually this task does not use the {@link InputOutputContext} for each job, but rather for the experiment as a whole
 * when determining the destination path for files. Similarly the message-log of the experiment is used for non-error messages.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>copy</td><td>yes</td><td>a copied file for each input file.</td></tr>
 * <tr><td rowspan="3"><i>outputs from the {@link RecordingCounter}</i></td></tr>
 * <tr><td rowspan="3"><i>outputs from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 * 
 * @param <T> shared-state of {@code naming}
 * @author Owen Feehan
 *
 */
public class CopyFiles<T> extends Task<FileWithDirectoryInput,RecordingCounter<T>> {

    /** 
     * Writes copy (according to {@code copyFilesMethod} of the file in the output directory with naming {@code copyFilesNaming}.
     * 
     * <p>If this output is disabled, a "dummy mode" occurs where files aren't copied, but instead a line is written into the experiment log with what would be the destination-path if a copy occurs.
     */
    private static final String OUTPUT_COPY = "copy";
       
    // START BEAN PROPERTIES
    /** How the copying occurs (e.g. with or without compression). */
    @BeanField @Getter @Setter private CopyFilesMethod method = new Bytewise();

    /** How an output name (and path) is selected for an input file. */
    @BeanField @Getter @Setter private CopyFilesNaming<T> naming;
    // END BEAN PROPERTIES
    
    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
    
    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(FileWithDirectoryInput.class);
    }

    @Override
    public RecordingCounter<T> beforeAnyJobIsExecuted(Outputter outputter,
            ConcurrencyPlan concurrencyPlan, List<FileWithDirectoryInput> inputs, ParametersExperiment params)
            throws ExperimentExecutionException {
        T namingSharedState = naming.beforeCopying(params.getOutputter().getOutputDirectory(), inputs.size());
        
        return new RecordingCounter<>(new Counter(), outputter, namingSharedState);
    }
    
    @Override
    public void doJobOnInput(InputBound<FileWithDirectoryInput, RecordingCounter<T>> input)
            throws JobExecutionException {
        // Determine a destination for the output, and create a corresponding logger
        Path destination = input.getContextExperiment().getOutputter().getOutputDirectory();
                
        try {
            copyFile(input.getInput().getDirectory(),
                destination,
                input.getInput().pathForBindingRequired().toFile(),
                input.getContextExperiment().getOutputter().outputsEnabled().isOutputEnabled(OUTPUT_COPY),
                input.getSharedState()
            );
        } catch (OperationFailedException | InputReadFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(RecordingCounter<T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        sharedState.closeLogger();
    }
    
    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_COPY);
    }
    
    private void copyFile(
            Path source,
            Path destinationDirectory,
            File file,
            boolean copyEnabled,
            RecordingCounter<T> recordingCounter
    ) throws OperationFailedException {

        try {
            int index = recordingCounter.incrementCounter();
            
            Optional<Path> destinationFile =
                    naming.destinationPath(source, destinationDirectory, file, index, recordingCounter.getNamingSharedState());
            
            if (destinationFile.isPresent() && copyEnabled) {
                method.makeCopy(file.toPath(), destinationFile.get());
            }
            
            recordingCounter.recordCopiedOutput(file.toPath().toAbsolutePath().normalize(), destinationFile, index);
            
        } catch (OutputWriteFailedException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
