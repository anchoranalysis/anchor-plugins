package org.anchoranalysis.plugin.io.outputmanager;

/*
 * #%L
 * anchor-io
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


import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.error.reporter.ErrorReporterIntoLog;
import org.anchoranalysis.experiment.log.ConsoleLogReporter;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.output.bean.OutputManagerWithPrefixer;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.FilePathCounter;
import org.anchoranalysis.plugin.io.bean.output.OutputManagerPermissive;

public class TempBoundOutputManager {

	private BoundOutputManagerRouteErrors boundOutputManager;
	
	//private static Log log = LogFactory.getLog(TempBoundOutputManager.class);
	
	private static TempBoundOutputManager instance = null;
	
	public static TempBoundOutputManager instance() {
		if (instance==null) {
			instance = new TempBoundOutputManager();
		}
		return instance;
	}
	
	private TempBoundOutputManager() {
		
		//new StaticFilePath("C:\\Users\\ofeehan\\ETHZ\\Data\\experiments\\debug_output_manager\\")
		
		Path pathTempFolder = FileSystems.getDefault().getPath( System.getProperty("java.io.tmpdir"), "AnchorOutput");

		System.out.printf("Temp output folder is: %s%n", pathTempFolder );
		
		//log.info("Constructor TempBoundOutputManager");
		boundOutputManager = outputManagerFor(pathTempFolder);
	}
	
	// These operations must occur before creating TempBoundOutputManager
	private static void globalSetup() {
		TestReaderWriterUtilities.ensureRasterWriter();		
	}
	
	public static BoundOutputManagerRouteErrors outputManagerFor( Path pathTempFolder ) {
		
		globalSetup();
		
		ErrorReporter errorReporter = new ErrorReporterIntoLog( new ConsoleLogReporter() );
		
		OutputWriteSettings ows = new OutputWriteSettings();
		
		// We populate any defaults in OutputWriteSettings from our default bean factory
		try {
			ows.checkMisconfigured( RegisterBeanFactories.getDefaultInstances() );
		} catch (BeanMisconfiguredException e1) {
			errorReporter.recordError(TempBoundOutputManager.class, e1);
		}
		
		FilePathCounter filePathCounter = new FilePathCounter( pathTempFolder.toString() );
		
		// A localPath is needed to allow the TempBoundOutputManager to be used
		localiseBean(filePathCounter, errorReporter, pathTempFolder);
		
		OutputManagerWithPrefixer outputManager = new OutputManagerPermissive();
		outputManager.setDelExistingFolder( true );
		outputManager.setOutputWriteSettings( ows );
		outputManager.setFilePathPrefixer( filePathCounter );
		
		try {
			return new BoundOutputManagerRouteErrors(
				outputManager.bindRootFolder( "debug", new ManifestRecorder(), new FilePathPrefixerParams(false, null) ),
				errorReporter
			);
		} catch (AnchorIOException e) {
			errorReporter.recordError(TempBoundOutputManager.class, e);
			return null;
		}		
	}

	public BoundOutputManagerRouteErrors getBoundOutputManager() {
		return boundOutputManager;
	}

	private static <T> void localiseBean( AnchorBean<T> bean, ErrorReporter errorReporter, Path pathTempFolder ) {
		try {
			bean.localise( pathTempFolder.resolve("fakeBean.xml") );
		} catch (BeanMisconfiguredException e1) {
			errorReporter.recordError(TempBoundOutputManager.class, e1);
		}
	}
	
}
