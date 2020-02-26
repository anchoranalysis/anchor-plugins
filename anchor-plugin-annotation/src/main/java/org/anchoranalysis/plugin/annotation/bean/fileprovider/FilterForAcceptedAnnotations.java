package org.anchoranalysis.plugin.annotation.bean.fileprovider;

/*
 * #%L
 * anchor-annotation
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.annotation.io.mark.MarkAnnotationReader;
import org.anchoranalysis.annotation.mark.MarkAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.params.InputContextParams;

public class FilterForAcceptedAnnotations extends FileProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FileProvider fileProvider;
	
	@BeanField
	private List<FilePathGenerator> listFilePathGenerator = new ArrayList<>();
	// END BEAN PROPERTIES

	private MarkAnnotationReader annotationReader = new MarkAnnotationReader(false);
	
	@Override
	public Collection<File> matchingFiles(ProgressReporter progressReporter, InputContextParams inputContext, LogErrorReporter logger)
			throws AnchorIOException {
		
		Collection<File> filesIn = fileProvider.matchingFiles(progressReporter, inputContext, logger);
		
		List<File> filesOut = new ArrayList<>();
		
		for( File f : filesIn ) {
			
			if (isFileAccepted(f)) {
				filesOut.add(f);
			}
			
		}
		
		return filesOut;
	}

	public List<FilePathGenerator> getListFilePathGenerator() {
		return listFilePathGenerator;
	}

	public void setListFilePathGenerator(
			List<FilePathGenerator> listFilePathGenerator) {
		this.listFilePathGenerator = listFilePathGenerator;
	}

	public FileProvider getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	private boolean isFileAccepted( File file) throws AnchorIOException {
		
		for( FilePathGenerator fpg : listFilePathGenerator ) {
			Path annotationPath = fpg.outFilePath( file.toPath(), false );
			
			MarkAnnotation annotation = annotationReader.read( annotationPath );
			
			if (annotation==null || !annotation.isAccepted()) {
				return false;
			}
		}
		return true;
	}

}
