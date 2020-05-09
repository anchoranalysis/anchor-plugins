package org.anchoranalysis.plugin.io.bean.provider.file;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;

public class FileProviderMultiple extends FileProvider {

	// START BEAN PROPERTIES
	@BeanField
	private List<FileProvider> list = new ArrayList<>();
	// END BEAN PROPERTIES
	
	@Override
	public Collection<File> matchingFiles(InputManagerParams params) throws AnchorIOException {

		try( ProgressReporterMultiple prm = new ProgressReporterMultiple(params.getProgressReporter(), list.size())) {
			
			List<File> combined = new ArrayList<>();
			
			for( FileProvider fp : list ) {
				
				ProgressReporterOneOfMany prLocal = new ProgressReporterOneOfMany(prm);
				combined.addAll(
					fp.matchingFiles(params.withProgressReporter(prLocal))
				);
				prm.incrWorker();
			}
			return combined;
		}
		

	}

	public List<FileProvider> getList() {
		return list;
	}

	public void setList(List<FileProvider> list) {
		this.list = list;
	}

}
