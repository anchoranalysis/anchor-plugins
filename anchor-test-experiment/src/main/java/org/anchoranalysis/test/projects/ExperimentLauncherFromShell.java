package org.anchoranalysis.test.projects;

/*
 * #%L
 * anchor-test-experiment
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


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.anchoranalysis.test.TestDataInitException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;
import org.junit.rules.TemporaryFolder;

/**
 * Launches an experiment by calling the application 'anchor' via the shell.
 * 
 * This is particularly useful for unit-tests that call experiments, but want to do it with all the libraries
 * and plugins instantiated, as they would be from the shell.
 * 
 *
 */
public class ExperimentLauncherFromShell {
	
	/**
	 * Command for calling anchor
	 */
	private static final String ANCHOR_COMMAND = "anchor";
	
	/**
	 * System-property key for the path to directory that contains the anchor distribution
	 */
	private static final String PATH_PROPERTY_IDENTIFIER = "anchor.distribution.path.test";
	
	
	private TestLoader testLoader;
	
	public ExperimentLauncherFromShell(TestLoader testLoader) {
		super();
		this.testLoader = testLoader;
	}

	/**
	 * The path we use for calling the anchor-executable
	 * 
	 * If a property with id (pathPropertyIdentifier) exists then an explicit
	 *   path is constructed.  Otherwise a relative path is used, relying
	 *   on the system path to find the correct executable.
	 * 
	 * @return a path to the anchor executable
	 */
	private String pathToAnchorExecutable() {
		
		String anchorCommand = SystemUtils.IS_OS_WINDOWS ? ANCHOR_COMMAND + ".exe" : ANCHOR_COMMAND;
		
		String pathProperty = System.getProperty(PATH_PROPERTY_IDENTIFIER);
		if (pathProperty==null) {
			// No explicit path specified, so we simply rely on the system-path
			return anchorCommand;
		} else {
			String pathCombined = Paths.get(pathProperty)
					.resolve("bin/" + anchorCommand)
					.toString();
			System.out.printf("Testing with path: %s (%s)%n", pathCombined, pathProperty);	// NOSONAR
			return pathCombined;
		}
	}

	
	/**
	 * Runs an experiment identified by an XML found in the resources
	 * 
	 * @param testPathToExperiment path to the resources where the experiment XML is found
	 */
	public void runExperiment( String testPathToExperiment ) {
		runExperiment(testPathToExperiment, null, null );
	}
	
	/**
	 * Creates a shell command for calling anchor
	 * 
	 * @param experimentPath the path to the experiment configuration
	 * @param inputPath if defined, the path to a replacement input manager. if empty(), ignored.
	 * @param outputPath if defined, the path to a replacement output manager. if empty(), ignored.
	 * @return a command to call anchor with appropripate arguments
	 */
	private CommandLine createShellCommand( Path experimentPath, Optional<Path> inputPath, Optional<Path> outputPath ) {

		CommandLine command = new CommandLine(pathToAnchorExecutable());
		command.addArgument( experimentPath.toString() );
		
		if (inputPath.isPresent()) {
			command.addArgument("-input");
			command.addArgument(inputPath.get().toString());
		}
		
		if (outputPath.isPresent()) {
			command.addArgument("-output");
			command.addArgument(outputPath.get().toString());
		}
		
		return command;
	}

	/**
	 * Runs an experiment identified by an XML found in the resources
	 * 
	 * @param testPathToExperiment path to the resources where the experiment XML is found
	 * @param testPathToInput if non-null, the path to a replacement input-manager. if null, ignored.
	 * @param testPathToOutput if non-null, the path to a replacement output-manager. if null, ignored.
	 */
	public void runExperiment( String testPathToExperiment, Optional<String> testPathToInput, Optional<String> testPathToOutput ) {
		
		// The command we pass to the shell
		CommandLine shellCmd = createShellCommand(
			testLoader.resolveTestPath( testPathToExperiment ),
			resolve(testPathToInput),
			resolve(testPathToOutput)
		);
		
		System.out.printf("Shell command: %s%n", shellCmd.toString());	// NOSONAR
		
		try {
			DefaultExecutor executor = new DefaultExecutor();
			
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			
			executor.setStreamHandler( new PumpStreamHandler() );
			executor.execute(shellCmd, resultHandler);
			resultHandler.waitFor();
			
		} catch (IOException e) {
			throw new TestDataLoadException(e);
		} catch (InterruptedException e) {
			// Restore interrupted state
			Thread.currentThread().interrupt();
		}
	}
	
	
	
	/**
	 * Copy files to the temporary-folder
	 * 
	 * @param temporaryFolder the destination temporary-folder to copy files to
	 * @param subdirectories if non-null, specific subdirectories to copy. if null, everything is copied.
	 * @return the temporary folder that has been created
	 */
	private void copyToTemporaryFolder( TemporaryFolder temporaryFolder, String[] subdirectories )  {
	    try {
			temporaryFolder.create();
		} catch (IOException e) {
			throw new TestDataInitException(e);
		}
	    
	    try {
	    	if (subdirectories!=null) {
	    		testLoader.copyToDirectory( subdirectories, temporaryFolder.getRoot());	
	    	} else {
	    		testLoader.copyToDirectory( temporaryFolder.getRoot() );
	    	}
		} catch (IOException e) {
			throw new TestDataInitException(e);
		}
	}
	
	
	
	/**
	 * Creates a temporary-folder, copies data  all data from the associated test-loader, and runs
	 * an experiment
	 * 
	 * @param testPathToExperiment path to the resources where the experiment XML is found
	 * @param temporaryFolder the temporary folder to copy files to
	 * @return a test-loader bounded to the temporary folder
	 */
	public TestLoader runExperimentInTemporaryFolder( String testPathToExperiment, TemporaryFolder temporaryFolder ) {
		return runExperimentInTemporaryFolder(testPathToExperiment,null,null,temporaryFolder, null);
	}
	
	
	/**
	 * Creates a temporary-folder, copies data  all data from the associated test-loader, and runs
	 * an experiment
	 *
	 * @param testPathToExperiment path to the resources where the experiment XML is found
	 * @param testPathToInput if defined, the path to a replacement input-manager. if empty(), ignored.
	 * @param testPathToOutput if defined, the path to a replacement output-manager. if empty(), ignored. 
	 * @param temporaryFolder the temporary folder to copy files to
	 * @param specificSubdirs if non-null, specific subdirectories to copy. if null, ignored.
	 * @return a test-loader bounded to the temporary folder
	 */
	public TestLoader runExperimentInTemporaryFolder(
		String testPathToExperiment,
		Optional<String> testPathToInput,
		Optional<String> testPathToOutput,
		TemporaryFolder temporaryFolder,
		String[] specificSubdirs
	) {
		
		copyToTemporaryFolder( temporaryFolder, specificSubdirs );
		
		TestLoader loaderTemporaryFolder = TestLoader.createFromExplicitDirectory( temporaryFolder.getRoot().toPath() );
				
		ExperimentLauncherFromShell launcherTemporaryFolder = new ExperimentLauncherFromShell(loaderTemporaryFolder);
		launcherTemporaryFolder.runExperiment(
			testPathToExperiment,
			testPathToInput,
			testPathToOutput
		);
	    	    
	    return TestLoader.createFromExplicitDirectory(temporaryFolder.getRoot().toPath());
	}
		
	
	/**
	 * Resolves a path iff it's defined.
	 * 
	 * @param testPath path to resolve
	 * @return resolved path or null
	 */
	private Optional<Path> resolve( Optional<String> testPath ) {
		return testPath.map( path->
			testLoader.resolveTestPath(path)
		);
	}
}
