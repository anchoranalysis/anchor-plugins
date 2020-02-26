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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.apache.commons.io.FilenameUtils;


/**
 * Removes extensions from the descriptive-name (but not from the file)
 * 
 * <p>As an exception, the extension can be retained if there is more than one file with the same descriptive-name</p>
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

	/** Keeps the extension if the file-name (without the extension) becomes duplicated with another */
	@BeanField
	private boolean preserveExtensionIfDuplicate = true;
	// END BEAN PROPERTIES

	public RemoveExtensions() {
		
	}
	
	public RemoveExtensions(DescriptiveNameFromFile descriptiveName) {
		this.descriptiveName = descriptiveName;
	}
	
	@Override
	public List<DescriptiveFile> descriptiveNamesFor(Collection<File> files, String elseName) {
		
		List<DescriptiveFile> df = descriptiveName.descriptiveNamesFor(files, elseName);
		
		if (preserveExtensionIfDuplicate) {
			return considerDuplicates(df);
		} else {
			return df;
		}
	}
	
	
	private static List<DescriptiveFile> considerDuplicates( List<DescriptiveFile> df ) {
		List<DescriptiveFile> dfWithoutExt = removeExt(df);
		
		// A count for each file, based upon the uniqueness of the description
		Map<String,Long> countedWithoutExt = dfWithoutExt.stream().collect(
			Collectors.groupingBy(
				d -> d.getDescriptiveName(),
				Collectors.counting()
			)
		);
		
		return listMaybeWithExtension(df, dfWithoutExt, countedWithoutExt);
	}
	
	
	/**
	 * Creates a new list selecting either the version with an extension (if there is more than 1 entry) or without an extension (otherwise)
	 * 
	 * @param listWith descriptive-names with the extension
	 * @param listWithout descriptive-names without the extension (in identical order to listWith)
	 * @param countWithout a corresponding count for each entry in listWithout
	 * @return a list in the same order, selecting either the corresponding entry from listWith or listWithout depending on the count value
	 */
	private static List<DescriptiveFile> listMaybeWithExtension(  List<DescriptiveFile> listWith,  List<DescriptiveFile> listWithout, Map<String,Long> countWithout) {

		List<DescriptiveFile> out = new ArrayList<>();
		
		// Now we iterate through both, and if the count is more than 1, then the extension is kept
		for( int i=0; i<listWith.size(); i++) {
			
			DescriptiveFile without = listWithout.get(i);
			long count = countWithout.get(without.getDescriptiveName());
			
			out.add(
				count > 1 ? listWith.get(i) : without 
			);
			
		}
		
		return out;
	}
	
	private static List<DescriptiveFile> removeExt( List<DescriptiveFile> df ) {
		return df.stream()
			.map( RemoveExtensions::removeExtension )
			.collect( Collectors.toList() );
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

	public boolean isPreserveExtensionIfDuplicate() {
		return preserveExtensionIfDuplicate;
	}

	public void setPreserveExtensionIfDuplicate(boolean preserveExtensionIfDuplicate) {
		this.preserveExtensionIfDuplicate = preserveExtensionIfDuplicate;
	}
}
