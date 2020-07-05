package org.anchoranalysis.anchor.plugin.quick.bean.structure;

/*-
 * #%L
 * anchor-plugin-quick
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.anchor.plugin.quick.bean.experiment.QuickMultiDatasetExperiment;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.io.IReplaceInputManager;
import org.anchoranalysis.experiment.io.IReplaceOutputManager;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Similar to {@link QuickMultiDatasetStructuredExperiment} put sets properties according to an expected directory structure.
 * <p>
 * <pre>  
 *   Project Directory/
 *      Experiments/			# where Experiments are stored
 *      Experiments/include/	# where a logReporterExperiment.xml and logReporterTask.xml can be found
 *      Filesets/				# where Filesets are stored
 *      IO/OutputManager/		# where OutputManagers are stored
 * </pre>
 * <p>
 * It also assumes a certain naming structure for the xml files in each, based upon an experiment-type parameter $1.
 * <pre>
 *		Filesets/For$1/		# the first letter of $1 is capitalized)
 *		IO/OutputManager/$1.xml
 * </pre>
 * <p>
 *	his allows many parameters of {@link QuickMultiDatasetStructuredExperiment} to be easily set.
 */
public class QuickMultiDatasetStructuredExperiment<T extends InputFromManager,S> extends Experiment implements IReplaceInputManager, IReplaceOutputManager {

	// START BEAN PROPERTIES
	/** How many directories to recurse back from the current file to the Experiments/ directory
	 * 
	 *  0 implies that the file is directly in Experiments/ 
	 *  1 implies that the file is in Experiments/SOMESUBDIR/ etc.
	 * */
	@BeanField @Getter @Setter
	private int directoryDistance = 0;
	
	/** The value of $1 (see comment above) in naming-structure. It should be in camel-case typically. */
	@BeanField @Getter @Setter
	private String experimentType;
	// END BEAN PROPERTIES
			
	private QuickMultiDatasetExperiment<T,S> delegate;
	
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
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		populateDelegateIfNeeded();
		delegate.checkMisconfigured(defaultInstances);
	}
	
	private void populateDelegateIfNeeded() {
		if (!populatedDelegate) {
			delegate.setFolderDataset( folderDataset() );
			delegate.setOutput( output() );
			delegate.setLogReporterExperimentPath( logReporterPath("Experiment") );
			delegate.setLogReporterTaskPath( logReporterPath("Task") );

			populatedDelegate = true;
		}
	}
	
	private String folderDataset() {
		return String.format(
			"%s../Filesets/For%s/",
			pathPrefix(),
			StringUtils.capitalize(experimentType)
		);
	}
	
	private String output() {
		return String.format(
			"%s../IO/OutputManager/%s.xml",
			pathPrefix(),
			experimentType
		);
	}
	
	private String logReporterPath( String suffix ) {
		return String.format(
			"%s/include/logReporter%s.xml",
			pathPrefix(),
			StringUtils.capitalize(suffix)
		);
	}
	
	private String pathPrefix() {
		if (directoryDistance==0) {
			return "./";
		} else {
			StringBuilder sb = new StringBuilder();
			for( int i=0; i<directoryDistance; i++) {
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
	public void replaceOutputManager(OutputManager outputManager) throws OperationFailedException {
		populateDelegateIfNeeded();
		delegate.replaceOutputManager(outputManager);
	}

	@Override
	public void replaceInputManager(InputManager<?> inputManager) throws OperationFailedException {
		populateDelegateIfNeeded();
		delegate.replaceInputManager(inputManager);
	}

	@Override
	public void doExperiment(ExperimentExecutionArguments expArgs) throws ExperimentExecutionException {
		populateDelegateIfNeeded();
		delegate.doExperiment(expArgs);
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

	public int getMaxNumProcessors() {
		return delegate.getMaxNumProcessors();
	}

	public void setMaxNumProcessors(int maxNumProcessors) {
		delegate.setMaxNumProcessors(maxNumProcessors);
	}

	public boolean isSuppressExceptions() {
		return delegate.isSuppressExceptions();
	}

	public void setSuppressExceptions(boolean suppressExceptions) {
		delegate.setSuppressExceptions(suppressExceptions);
	}
}
