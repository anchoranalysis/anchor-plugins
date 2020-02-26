package org.anchoranalysis.plugin.io.bean.experiment;

/*
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.log.ConsoleLogReporter;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.CopyFilesMethod;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.SimpleCopy;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.CopyFilesNaming;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.PreserveName;
import org.anchoranalysis.plugin.io.bean.filepath.FilePath;
import org.apache.commons.io.FileUtils;

public class CopyFilesExperiment extends Experiment {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FileProvider fileProvider;
	
	@BeanField
	private FilePath sourceFolderPath;
	
	@BeanField
	private FilePath destinationFolderPath;
	
	@BeanField
	private boolean dummyMode = false;
	
	@BeanField
	private CopyFilesMethod copyFilesMethod = new SimpleCopy();
	
	@BeanField
	private CopyFilesNaming copyFilesNaming = new PreserveName();
	
	@BeanField
	private ExperimentIdentifier experimentIdentifier = null;
	
	@BeanField
	private boolean delExistingFolder = true;
	// END BEAN PROPERTIES
	
	@Override
	public void doExperiment(ExperimentExecutionArguments expArgs)
			throws ExperimentExecutionException {
		
		System.out.print("Reading files: ");
		
		try {
			doCopying(
				findMatchingFiles(expArgs),
				sourceFolderPath.path(expArgs.isDebugEnabled()),
				destinationFolderPath.path(expArgs.isDebugEnabled())
			);
			
		} catch (AnchorIOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public boolean useDetailedLogging() {
		return true;
	}
	
	private void doCopying( Collection<File> files, Path sourcePath, Path destPath) throws ExperimentExecutionException {
		
		ProgressReporter progressReporter = createProgressReporter(files.size());
		
		if (!dummyMode) {
			System.out.print("Copying files: ");
			if (delExistingFolder) {
				FileUtils.deleteQuietly( destPath.toFile() );
			}
			destPath.toFile().mkdirs();
		}
		
		progressReporter.open();
	
		try {
			copyFilesNaming.beforeCopying(destPath, files.size() );
			
			int i = 0;
			for( File f : files) {
				copyFile(sourcePath, destPath, f, i++, progressReporter);
			}
			
			copyFilesNaming.afterCopying(destPath, dummyMode);
			
		} catch (AnchorIOException | OperationFailedException e) {
			throw new ExperimentExecutionException(e);
		} finally {
			progressReporter.close();
		}
	}
	
	private ProgressReporter createProgressReporter( int numFiles ) {
		ProgressReporter progressReporter = dummyMode ? ProgressReporterNull.get() : new ProgressReporterConsole(5);
		progressReporter.setMin(0);
		progressReporter.setMax(numFiles-1);
		return progressReporter;
	}
	
	private void copyFile( Path sourcePath, Path destPath, File file, int iter, ProgressReporter progressReporter ) throws OperationFailedException {

		try {
			Path destination = copyFilesNaming.destinationPath(sourcePath, destPath, file, iter);
			
			// Skip any files with a NULL destinationPath
			if (destination==null) {
				if (dummyMode) {
					System.out.printf("Skipping %s%n", file.getPath() );
				}
				return;
			}
			
			if (dummyMode) {
				System.out.printf("Copying %s to %s%n", file.getPath(), destination.toString() );		
			} else {
				copyFilesMethod.createDestinationFile(file.toPath(), destination);
			}
		} catch (AnchorIOException | CreateException e) {
			throw new OperationFailedException(e);
		} finally {
			progressReporter.update(iter);
		}
		
	}
	
	private Collection<File> findMatchingFiles(ExperimentExecutionArguments expArgs) throws ExperimentExecutionException {
		Collection<File> files;
		try {
			files = fileProvider.matchingFiles(
				new ProgressReporterConsole(5),
				expArgs.createInputContext(),
				new LogErrorReporter( new ConsoleLogReporter() )	// Print errors to the screen
				
			);
		} catch (AnchorIOException e) {
			throw new ExperimentExecutionException("Cannot find input files", e);
		} catch (IOException e) {
			throw new ExperimentExecutionException("Cannot create input context", e);
		}
		
		assert(files!=null);
		return files;
	}
		
	public FileProvider getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	public boolean isDummyMode() {
		return dummyMode;
	}

	public void setDummyMode(boolean dummyMode) {
		this.dummyMode = dummyMode;
	}

	public ExperimentIdentifier getExperimentIdentifier() {
		return experimentIdentifier;
	}

	public void setExperimentIdentifier(ExperimentIdentifier experimentIdentifier) {
		this.experimentIdentifier = experimentIdentifier;
	}


	public FilePath getSourceFolderPath() {
		return sourceFolderPath;
	}


	public void setSourceFolderPath(FilePath sourceFolderPath) {
		this.sourceFolderPath = sourceFolderPath;
	}


	public FilePath getDestinationFolderPath() {
		return destinationFolderPath;
	}


	public void setDestinationFolderPath(FilePath destinationFolderPath) {
		this.destinationFolderPath = destinationFolderPath;
	}


	public CopyFilesNaming getCopyFilesNaming() {
		return copyFilesNaming;
	}


	public void setCopyFilesNaming(CopyFilesNaming copyFilesNaming) {
		this.copyFilesNaming = copyFilesNaming;
	}


	public CopyFilesMethod getCopyFilesMethod() {
		return copyFilesMethod;
	}


	public void setCopyFilesMethod(CopyFilesMethod copyFilesMethod) {
		this.copyFilesMethod = copyFilesMethod;
	}


	public boolean isDelExistingFolder() {
		return delExistingFolder;
	}


	public void setDelExistingFolder(boolean delExistingFolder) {
		this.delExistingFolder = delExistingFolder;
	}
}
