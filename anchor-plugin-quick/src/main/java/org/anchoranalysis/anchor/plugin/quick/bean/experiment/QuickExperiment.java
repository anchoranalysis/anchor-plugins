package org.anchoranalysis.anchor.plugin.quick.bean.experiment;

import java.io.IOException;

/*
 * #%L
 * anchor-plugin-mpp-experiment
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


import java.nio.file.Path;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.BeanXmlLoader;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.bean.xml.factory.BeanPathUtilities;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifierConstant;
import org.anchoranalysis.experiment.bean.io.InputOutputExperiment;
import org.anchoranalysis.experiment.bean.log.ToConsole;
import org.anchoranalysis.experiment.bean.processor.SequentialProcessor;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.bean.provider.file.SearchDirectory;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.bean.allowed.AllOutputAllowed;
import org.anchoranalysis.io.output.bean.allowed.OutputAllowed;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.DirectoryStructure;
import org.anchoranalysis.plugin.io.bean.input.file.Files;
import org.anchoranalysis.plugin.io.bean.input.stack.Stacks;
import org.anchoranalysis.plugin.mpp.experiment.bean.outputmanager.OutputManagerStack;

import lombok.Getter;
import lombok.Setter;

/**
 * A quick way of defining an InputOutputExperiment where several assumptions are made.
 * 
 * TODO remove the explicit setting of setWriteRasterMetadata
 * 
 * @author Owen Feehan
 * @param <S> shared-state
 *
 */
public class QuickExperiment<S> extends Experiment {

	// START BEAN PROPERTIES
	/**
	 * A string indicating the input file(s)
	 * 
	 * Either:
	 *   1. a file-path to a single image
	 *   2. a file glob matching several images (e.g. /somedir/somefile*.png)
	 *   3. a file-path ending in .xml or .XML. This is then interpreted
	 *     treated a a paths to BeanXML describing a NamedMultiCollectionInputManager 
	 */
	@BeanField @Getter @Setter
	private String fileInput;
	
	@BeanField @Getter @Setter
	private String folderOutput;
	
	@BeanField @Getter @Setter
	private Task<MultiInput,S> task;
	
	@BeanField @Getter @Setter
	private String inputName = "stackInput";
	
	@BeanField @Getter @Setter
	private OutputAllowed outputEnabled = new AllOutputAllowed();
	
	@BeanField @Getter @Setter
	private OutputAllowed objects = new AllOutputAllowed();
	
	@BeanField @Getter @Setter
	private OutputAllowed stackCollectionOutputEnabled = new AllOutputAllowed();
	
	@BeanField @Getter @Setter
	private OutputWriteSettings outputWriteSettings = new OutputWriteSettings();
	// END BEAN PROPERTIES

	// Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks elsewhere
	private BeanInstanceMap defaultInstances;
		
	private InputOutputExperiment<MultiInput,S> delegate;
	
	private ExperimentIdentifierConstant experimentIdentifier = new ExperimentIdentifierConstant("single", "1.0");
	
	public QuickExperiment() {
		delegate = new InputOutputExperiment<>();
		delegate.setExperimentIdentifier(experimentIdentifier);		
	}
		
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
	}

	private InputManager<MultiInput> createInputManagerBean( Path beanPath ) throws ExperimentExecutionException {
		try {
			return BeanXmlLoader.loadBean(beanPath);
		} catch (BeanXmlException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	
	private InputManager<MultiInput> createInputManagerImageFile( SearchDirectory fs ) {
		return new MultiInputManager(
			inputName,
			new Stacks(
				new Files(fs)
			)
		);
	}
	
	private OutputManager createOutputManager( Path inPathBaseDir ) {
		
		Path pathFolderOut = BeanPathUtilities.pathRelativeToBean(this, folderOutput);
		
		OutputManagerStack outputManager = new OutputManagerStack();
		outputManager.setSilentlyDeleteExisting(true);
		outputManager.setOutputEnabled( outputEnabled );
		outputManager.setObjects(objects);
		outputManager.setStackCollectionOutputEnabled( stackCollectionOutputEnabled );
		try {
			outputManager.localise(getLocalPath());
		} catch (BeanMisconfiguredException e) {
			// Should never arise, as getLocalPath() should always be absolute
			assert(false);
		}
		
		DirectoryStructure filePathRslvr = new DirectoryStructure();
		filePathRslvr.setInPathPrefix( inPathBaseDir.toString() );
		filePathRslvr.setOutPathPrefix(pathFolderOut.toString());
		try {
			filePathRslvr.localise(getLocalPath());
		} catch (BeanMisconfiguredException e) {
			// Should never arise, as getLocalPath() should always be absolute
			assert(false);
		}
		outputManager.setFilePathPrefixer(filePathRslvr);
		outputManager.setOutputWriteSettings(outputWriteSettings);
		return outputManager;
	}
	
	@Override
	public void doExperiment(ExperimentExecutionArguments arguments)
			throws ExperimentExecutionException {
		delegate.associateXml( getXMLConfiguration() );
		
		Path combinedFileFilter = BeanPathUtilities.pathRelativeToBean(this, fileInput);
		
		if (combinedFileFilter.endsWith(".xml") || combinedFileFilter.endsWith(".XML")) {
			
			// Creates from an XML bean
			delegate.setInput( createInputManagerBean(combinedFileFilter) );
			
			Path outBasePath = combinedFileFilter.getParent();
			delegate.setOutput( createOutputManager(outBasePath) );
			
		} else {
			// Creates from a file
			SearchDirectory fs = new SearchDirectory();
			fs.setFileFilterAndDirectory(combinedFileFilter);
			
			delegate.setInput( createInputManagerImageFile(fs) );
			
			try {
				delegate.setOutput( createOutputManager(fs.getDirectoryAsPathEnsureAbsolute( arguments.createInputContext() )) );
				
			} catch (IOException e) {
				throw new ExperimentExecutionException(e);
			}
		}
		
		// Log Reporter
		delegate.setLogExperiment( new ToConsole() );
		
		
		// Task
		SequentialProcessor<MultiInput,S> taskProcessor = new SequentialProcessor<>();
		taskProcessor.setTask(task);
		delegate.setTaskProcessor(taskProcessor);
		
		try {
			delegate.checkMisconfigured( defaultInstances );
		} catch (BeanMisconfiguredException e) {
			throw new ExperimentExecutionException(e);
		}
		
		delegate.doExperiment(arguments);
	}

	@Override
	public boolean useDetailedLogging() {
		return delegate.useDetailedLogging();
	}
}
