package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

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

import org.anchoranalysis.anchor.plugin.quick.bean.filepath.FilePathGeneratorCollapseFileName;
import org.anchoranalysis.anchor.plugin.quick.bean.filepath.FilePathGeneratorRemoveTrailingDir;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorRegEx;
import org.anchoranalysis.io.bean.filepath.generator.Rooted;

public abstract class FilePathAppendBase extends AnchorBean<FilePathAppendBase> {

	// START BEAN FIELDS
	/** The name of the appended entity */
	@BeanField
	private String name;
	
	/** A suffix appended to the dataset name extracted from the reg exp */
	@BeanField
	private String datasetSuffix;

	/** A folder identifying the type of experiment (where the outputs are all put in the same directory */
	@BeanField
	private String experimentType;
		
	// If non-zero, n trailing directories are removed from the end 
	@BeanField
	private int trimTrailingDirectory = 0;
	
	@BeanField
	private int skipFirstTrim = 0;
	
	// Do not include the file-name
	@BeanField
	private boolean skipFileName = false;
	
	// Iff true, then the filename will be removed apart from the .extension which remains
	//  Specifically path/name.ext becomes path.ext
	@BeanField
	private boolean collapseFilename = false;
	// END BEAN FIELDS
	
	protected abstract String createOutPathString() throws BeanMisconfiguredException;

	/*** The first-part of the out-path string including the file-name of the previous experiment as a folder */
	protected String firstPartWithFilename() {
		return firstPartWithCustomEnd("/$3");
	}
	
	/*** The first-part of the out-path string including the dataset of the current experiment as a folder */
	protected String firstPartWithDataset() {
		return firstPartWithCustomEnd("/$2");
	}
	
	protected String firstPart() {
		return firstPartWithCustomEnd("");
	}
	
	private String firstPartWithCustomEnd( String end) {
		return String.format(
			"$1/experiments/%s/$2%s%s",
			getExperimentType(),
			getDatasetSuffix(),
			end
		);
	}

	protected String firstPartWithCustomMiddle(String middle) {
		return String.format(
			"$1/experiments/%s/$2%s%s/$2",
			getExperimentType(),
			getDatasetSuffix(),
			middle
		);
	}
	
	
	/**
	 * Creates a (rooted) file-path generator for a rootName and a regEx which matches three groups
	 * The first group should correspond to top-level folder for the project
	 * The second group should correspond to the unique name of the dataset.
	 * The third group should correspond to the unique name of the experiment.
	 *   
	 * @param rootName if non-empty (and non-NULL) a rooted filePathGenerator is created instead of a non rooted
	 * @param regEx
	 * @return
	 * @throws BeanMisconfiguredException
	 */
	public NamedBean<FilePathGenerator> createFilePathGenerator( String rootName, String regEx ) throws BeanMisconfiguredException {

		FilePathGenerator fpg = createRegEx(regEx);
		
		if (rootName!=null && !rootName.isEmpty()) {
			fpg = addRoot(fpg, rootName );
		}
		
		if (trimTrailingDirectory > 0 || skipFileName) {
			fpg = addRemoveTrailing(fpg);
		}
		
		if (collapseFilename) {
			fpg = addCollapse(fpg);
		}
		
		// Wrap in a named bean
		return new NamedBean<>(name,fpg);
	}
	
	private FilePathGenerator addCollapse( FilePathGenerator fpg )  {
		FilePathGeneratorCollapseFileName fpgCollapse = new FilePathGeneratorCollapseFileName();
		fpgCollapse.setFilePathGenerator(fpg);
		return fpgCollapse;
	}
	
	private FilePathGenerator addRemoveTrailing( FilePathGenerator fpg ) {
		FilePathGeneratorRemoveTrailingDir remove = new FilePathGeneratorRemoveTrailingDir();
		remove.setFilePathGenerator(fpg);
		remove.setTrimTrailingDirectory(trimTrailingDirectory);
		remove.setSkipFirstTrim(skipFirstTrim);
		return remove;
	}
	
	private static FilePathGenerator addRoot( FilePathGenerator fpg, String rootName ) {
		// Rooted File-Path
		Rooted delegate = new Rooted();
		delegate.setRootName(rootName);
		delegate.setItem(fpg);
		return delegate;
	}
	
	private FilePathGenerator createRegEx( String regEx ) throws BeanMisconfiguredException {
		
		// File path generator
		FilePathGeneratorRegEx fpg = new FilePathGeneratorRegEx();
		fpg.setRegEx(regEx);
		fpg.setOutPath( createOutPathString() );
		return fpg;
	}

	public String getExperimentType() {
		return experimentType;
	}

	public void setExperimentType(String experimentType) {
		this.experimentType = experimentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatasetSuffix() {
		return datasetSuffix;
	}

	public void setDatasetSuffix(String datasetSuffix) {
		this.datasetSuffix = datasetSuffix;
	}

	public int getTrimTrailingDirectory() {
		return trimTrailingDirectory;
	}

	public void setTrimTrailingDirectory(int trimTrailingDirectory) {
		this.trimTrailingDirectory = trimTrailingDirectory;
	}

	public int getSkipFirstTrim() {
		return skipFirstTrim;
	}

	public void setSkipFirstTrim(int skipFirstTrim) {
		this.skipFirstTrim = skipFirstTrim;
	}

	public boolean isSkipFileName() {
		return skipFileName;
	}

	public void setSkipFileName(boolean skipFileName) {
		this.skipFileName = skipFileName;
	}

	public boolean isCollapseFilename() {
		return collapseFilename;
	}

	public void setCollapseFilename(boolean collapseFilename) {
		this.collapseFilename = collapseFilename;
	}

}
