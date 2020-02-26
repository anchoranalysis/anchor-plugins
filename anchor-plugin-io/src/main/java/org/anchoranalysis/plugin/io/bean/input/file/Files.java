package org.anchoranalysis.plugin.io.bean.input.file;

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


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.params.InputContextParams;
import org.anchoranalysis.plugin.io.bean.descriptivename.PatternSpan;
import org.anchoranalysis.plugin.io.bean.descriptivename.RemoveExtensions;


/**
 * File-paths 
 * 
 * @author Owen Feehan
 *
 */
public class Files extends InputManager<FileInput> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3838832669433747423L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FileProvider fileProvider = null;
	
	@BeanField
	private DescriptiveNameFromFile descriptiveNameFromFile = new RemoveExtensions( new PatternSpan() );
	// END BEAN PROPERTIES

	@Override
	public List<FileInput> inputObjects(InputContextParams inputContext, ProgressReporter progressReporter, LogErrorReporter logger) throws AnchorIOException {
		
		Collection<File> files = getFileProvider().matchingFiles(progressReporter, inputContext, logger);
			
		return descriptiveNameFromFile.descriptiveNamesFor(files, "<unknown>").stream().map(
			df -> new FileInput(df)
		).collect( Collectors.toList() );
	}
	
	public FileProvider getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProvider fileSet) {
		this.fileProvider = fileSet;
	}

	public DescriptiveNameFromFile getDescriptiveNameFromFile() {
		return descriptiveNameFromFile;
	}

	public void setDescriptiveNameFromFile(
			DescriptiveNameFromFile descriptiveNameFromFile) {
		this.descriptiveNameFromFile = descriptiveNameFromFile;
	}
}
