package org.anchoranalysis.plugin.io.bean.descriptivename;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.apache.commons.io.FilenameUtils;


/**
 * Removes extensions from the descriptive-name (but not from the file)
 * 
 * @author owen
 *
 */
public class RemoveExtensions extends DescriptiveNameFromFile {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private DescriptiveNameFromFile descriptiveName;
	// END BEAN PROPERTIES

	public RemoveExtensions() {
		
	}
	
	public RemoveExtensions(DescriptiveNameFromFile descriptiveName) {
		this.descriptiveName = descriptiveName;
	}
	
	@Override
	public List<DescriptiveFile> descriptiveNamesFor(Collection<File> files, String elseName) {
		
		List<DescriptiveFile> df = descriptiveName.descriptiveNamesFor(files, elseName);
		return df
			.stream()
			.map( RemoveExtensions::removeExtension )
			.collect( Collectors.toList()
		);
	}

	public DescriptiveNameFromFile getDescriptiveName() {
		return descriptiveName;
	}

	public void setDescriptiveName(DescriptiveNameFromFile descriptiveName) {
		this.descriptiveName = descriptiveName;
	}
	
	private static DescriptiveFile removeExtension( DescriptiveFile df ) {
		return new DescriptiveFile(
			df.getFile(),
			FilenameUtils.removeExtension(df.getDescriptiveName())
		);
	}
}