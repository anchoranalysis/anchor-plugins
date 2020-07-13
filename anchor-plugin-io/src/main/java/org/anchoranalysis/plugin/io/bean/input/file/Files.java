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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.RemoveExtensions;
import org.anchoranalysis.plugin.io.bean.descriptivename.patternspan.PatternSpan;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * File-paths 
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor
public class Files extends InputManager<FileInput> {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private FileProvider fileProvider;
	
	@BeanField @Getter @Setter
	private DescriptiveNameFromFile descriptiveNameFromFile = new RemoveExtensions( new PatternSpan() );
	// END BEAN PROPERTIES

	public Files(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}
	
	@Override
	public List<FileInput> inputObjects(InputManagerParams params) throws AnchorIOException {
		try {
			Collection<File> files = getFileProvider().create(params);
				
			return FunctionalList.mapToList( 
				descriptiveNameFromFile.descriptiveNamesForCheckUniqueness(files, params.getLogger()),
				FileInput::new
			);
		} catch (FileProviderException e) {
			throw new AnchorIOException("Cannot find files", e);
		}
	}
}
