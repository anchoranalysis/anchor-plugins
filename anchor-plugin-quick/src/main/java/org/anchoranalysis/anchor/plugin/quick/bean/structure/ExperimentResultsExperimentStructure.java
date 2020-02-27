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

import java.io.File;
import java.util.Collection;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.file.matcher.MatchGlob;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.bean.provider.file.SearchDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.io.bean.provider.file.RootedFileSet;

/**
 * Finds some files produced in a previous experiment assuming a certain structure
 * 
 * A convenience method for commonly used prefixer settings when the output
 *   occurs in an experiment/$1/ file-system structure where $1 is the experimentType
 *
 */
public class ExperimentResultsExperimentStructure extends FileProvider {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** The name of the experiment including version suffix */
	@BeanField
	private String datasetName;
	
	/** A version-suffix appended to the dataset name extracted from the reg exp */
	@BeanField
	private String datasetVersion;

	/** A folder identifying the type of experiment (where the outputs are all put in the same directory */
	@BeanField
	private String experimentType;
	
	/** Root-name assuming multi-rooted strucutre */
	@BeanField
	private String rootName;
	
	/** Files to search for */
	@BeanField
	private String fileFilter = "*";
	
	/** If true the directory structure is ROOT/experiments/${ROOT_NAME}/${DATASET_NAME}_${DATASET_SUFFIX}
	 *  If false, the directory structure is ROOT/experiments/${DATASET_NAME}_${DATASET_SUFFIX}
	 */
	@BeanField
	private boolean rootInStructure = false;
	
	/** If rootInStructure==TRUE, the rootName is placed in the folder structure. If this is non-empty, a different rootName is inserted into this structure instead of the default rootName */
	@BeanField @AllowEmpty
	private String rootNameForStructure = "";
	
	/**
	 * If non-empty than an additional sub-directory is appended as a suffix ${DATASET_NAME}_${DATASET_SUFFIX}/${SUBDIRECTORY}/
	 */
	@BeanField @AllowEmpty
	private String subdirectory = "";
	
	/**
	 * If true the datasetName is appended as a sub-directory e.g. ${DATASET_NAME}_${DATASET_SUFFIX}/${DATASET_NAME}
	 */
	@BeanField
	private boolean datasetNameSubdirectory = false;
	
	/**
	 * Whether to apply the search recursively or not
	 */
	@BeanField
	private boolean recursive = false;
	
	@BeanField
	private int maxDirectoryDepth;
	// END BEAN PROPERTIES

	private RootedFileSet delegate;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
				
		delegate = createRootedFileSet();
		delegate.checkMisconfigured(defaultInstances);
		
		if (!subdirectory.isEmpty() && datasetNameSubdirectory) {
			throw new BeanMisconfiguredException("Both subdirectory and datasetNameSubdirectory should not be set");
		}
	}
	
	private RootedFileSet createRootedFileSet() {
		RootedFileSet out = new RootedFileSet();
		out.setDisableDebugMode(true);
		out.setRootName(rootName);
		out.setFileSet( createFiles() );
		return out;
	}
	
	private FileProviderWithDirectory createFiles() {
		SearchDirectory out = new SearchDirectory();
		out.setDirectory( new DirectoryCreator().apply() );
		out.setMatcher( new MatchGlob(fileFilter) );
		out.setRecursive(recursive);
		out.setMaxDirectoryDepth(maxDirectoryDepth);
		return out;
	}
	
	private class DirectoryCreator {
	
		public String apply() {
			StringBuilder sb = new StringBuilder("experiments/");
	
			if (rootInStructure) {
				addRootName(sb);
			}
			
			addExperimentType(sb);
			addDataset(sb);
			
			return sb.toString();
		}
		
		private void addRootName( StringBuilder sb ) {
			if (!rootNameForStructure.isEmpty()) {
				sb.append(rootNameForStructure);
			} else {
				sb.append(rootName);
			}
			sb.append("/");
		}
		
		private void addExperimentType( StringBuilder sb ) {
			sb.append(experimentType);
			sb.append("/");
		}
		
		private void addDataset( StringBuilder sb ) {
			sb.append(datasetName);
			sb.append("_");
			sb.append(datasetVersion);
			sb.append("/");
			
			if (!subdirectory.isEmpty()) {
				sb.append(subdirectory);
				sb.append("/");
			}
			
			if (datasetNameSubdirectory) {
				sb.append(datasetName);
				sb.append("/");
			}
		}
	}
		
	@Override
	public Collection<File> matchingFiles(InputManagerParams params) throws AnchorIOException {
		return delegate.matchingFiles(params);
	}

	public String getExperimentType() {
		return experimentType;
	}

	public void setExperimentType(String experimentType) {
		this.experimentType = experimentType;
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public String getFileFilter() {
		return fileFilter;
	}

	public void setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public boolean isRootInStructure() {
		return rootInStructure;
	}

	public void setRootInStructure(boolean rootInStructure) {
		this.rootInStructure = rootInStructure;
	}

	public boolean isDatasetNameSubdirectory() {
		return datasetNameSubdirectory;
	}

	public void setDatasetNameSubdirectory(boolean datasetNameSubdirectory) {
		this.datasetNameSubdirectory = datasetNameSubdirectory;
	}

	public String getDatasetVersion() {
		return datasetVersion;
	}

	public void setDatasetVersion(String datasetVersion) {
		this.datasetVersion = datasetVersion;
	}

	public String getSubdirectory() {
		return subdirectory;
	}

	public void setSubdirectory(String subdirectory) {
		this.subdirectory = subdirectory;
	}

	public String getRootNameForStructure() {
		return rootNameForStructure;
	}

	public void setRootNameForStructure(String rootNameForStructure) {
		this.rootNameForStructure = rootNameForStructure;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public int getMaxDirectoryDepth() {
		return maxDirectoryDepth;
	}

	public void setMaxDirectoryDepth(int maxDirectoryDepth) {
		this.maxDirectoryDepth = maxDirectoryDepth;
	}
}
