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

package org.anchoranalysis.plugin.io.bean.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.log.ToConsole;
import org.anchoranalysis.experiment.log.ConsoleMessageLogger;
import org.anchoranalysis.experiment.log.StatefulMessageLogger;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.files.FilesProviderException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.BindFailedException;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.CopyFilesMethod;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.SimpleCopy;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.CopyFilesNaming;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.PreserveName;
import org.anchoranalysis.plugin.io.bean.filepath.FilePath;
import org.apache.commons.io.FileUtils;

public class CopyFilesExperiment extends Experiment {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilesProvider filesProvider;

    @BeanField @Getter @Setter private FilePath sourceDirectoryPath;

    @BeanField @Getter @Setter private FilePath destinationDirectoryPath;

    @BeanField @Getter @Setter private boolean dummyMode = false;

    @BeanField @Getter @Setter private CopyFilesMethod copyFilesMethod = new SimpleCopy();

    @BeanField @Getter @Setter private CopyFilesNaming copyFilesNaming = new PreserveName();

    @BeanField @Getter @Setter private ExperimentIdentifier experimentIdentifier = null;

    @BeanField @Getter @Setter private boolean silentlyDeleteExisting = true;

    @BeanField @Getter @Setter private LoggingDestination log = new ToConsole();
    // END BEAN PROPERTIES

    @Override
    public void executeExperiment(ExperimentExecutionArguments arguments)
            throws ExperimentExecutionException {

        // Determine a destination for the output, and create a corresponding logger
        Path destination = determineDestination(arguments.isDebugModeEnabled());

        StatefulMessageLogger logger;
        try {
            logger = createLoggerFor(destination, arguments);
        } catch (BindFailedException e) {
            throw new ExperimentExecutionException(e);
        }

        logger.log("Reading files: ");

        try {
            doCopying(
                    findMatchingFiles(arguments),
                    sourceDirectoryPath.path(arguments.isDebugModeEnabled()),
                    destination,
                    logger);
            logger.close(true);
        } catch (InputReadFailedException e) {
            logger.close(false);
            throw new ExperimentExecutionException(e);
        }
    }

    private Path determineDestination(boolean debugEnabled) throws ExperimentExecutionException {
        try {
            return destinationDirectoryPath.path(debugEnabled);
        } catch (InputReadFailedException exc) {
            throw new ExperimentExecutionException("Cannot determine destination directory", exc);
        }
    }

    private StatefulMessageLogger createLoggerFor(
            Path destination, ExperimentExecutionArguments arguments) throws BindFailedException {
        return log.createWithConsoleFallback(
                OutputterChecked.createForDirectoryPermissive(destination, silentlyDeleteExisting),
                arguments,
                false);
    }

    @Override
    public boolean useDetailedLogging() {
        return true;
    }

    private void doCopying(
            Collection<File> files, Path sourcePath, Path destPath, MessageLogger logger)
            throws ExperimentExecutionException {

        ProgressReporter progressReporter = createProgressReporter(files.size());

        if (!dummyMode) {
            logger.log("Copying files: ");
            if (silentlyDeleteExisting) {
                FileUtils.deleteQuietly(destPath.toFile());
            }
            destPath.toFile().mkdirs();
        }

        progressReporter.open();

        try {
            copyFilesNaming.beforeCopying(destPath, files.size());

            int i = 0;
            for (File f : files) {
                copyFile(sourcePath, destPath, f, i++, progressReporter, logger);
            }

            copyFilesNaming.afterCopying(destPath, dummyMode);

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new ExperimentExecutionException(e);
        } finally {
            progressReporter.close();
        }
    }

    private ProgressReporter createProgressReporter(int numFiles) {
        ProgressReporter progressReporter =
                dummyMode ? ProgressReporterNull.get() : new ProgressReporterConsole(5);
        progressReporter.setMin(0);
        progressReporter.setMax(numFiles - 1);
        return progressReporter;
    }

    private void copyFile(
            Path sourcePath,
            Path destPath,
            File file,
            int iter,
            ProgressReporter progressReporter,
            MessageLogger logger)
            throws OperationFailedException {

        try {
            Optional<Path> destination =
                    copyFilesNaming.destinationPath(sourcePath, destPath, file, iter);

            // Skip any files with a null destinationPath
            if (!destination.isPresent()) {
                if (dummyMode) {
                    logger.logFormatted("Skipping %s%n", file.getPath());
                }
                return;
            }

            if (dummyMode) {
                logger.logFormatted("Copying %s to %s%n", file.getPath(), destination.toString());
            } else {
                copyFilesMethod.createDestinationFile(file.toPath(), destination.get());
            }
        } catch (OutputWriteFailedException | CreateException e) {
            throw new OperationFailedException(e);
        } finally {
            progressReporter.update(iter);
        }
    }

    private Collection<File> findMatchingFiles(ExperimentExecutionArguments expArgs)
            throws ExperimentExecutionException {
        try {
            return filesProvider.create(
                    new InputManagerParams(
                            expArgs.createInputContext(),
                            new ProgressReporterConsole(5),
                            new Logger(new ConsoleMessageLogger()) // Print errors to the screen
                            ));
        } catch (FilesProviderException e) {
            throw new ExperimentExecutionException("Cannot find input files", e);
        } catch (IOException e) {
            throw new ExperimentExecutionException("Cannot create input context", e);
        }
    }
}
