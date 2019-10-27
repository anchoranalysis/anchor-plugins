package org.anchoranalysis.plugin.io.bean.summarizer.path;

/*-
 * #%L
 * anchor-plugin-io
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.apache.commons.io.FilenameUtils;

/** Remembers each unique extension, and associated count */
public class ExtensionCount extends Summarizer<Path> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String NO_EXTENSION = "NO_EXTENSION";
	
	private Map<String,Integer> map = new TreeMap<String,Integer>();
	
	@Override
	public void add( Path fullFilePath ) {
		assert(fullFilePath!=null);
		
		String extension = FilenameUtils.getExtension(
			fullFilePath.toString()
		);
		
		incrCount(
			tidyExtension(extension)
		);
	}
	
	// Describes all the extensions found
	@Override
	public synchronized String describe() {
		
		int numKeys = map.keySet().size();
		
		if (numKeys==0) {
			return "No inputs have been found yet.";
		} else if (numKeys==1) {
			return String.format(
				"All inputs have extension .%s",
				map.keySet().iterator().next()
			);
		} else {
			return describeMultipleExtensions();
		}
	}
	
	private String describeMultipleExtensions() {
		StringBuilder sb = new StringBuilder();
		sb.append("Inputs have diverse extensions:");
		
		for( Entry<String,Integer> entry : map.entrySet() ) {
			sb.append(
				String.format(" .%s(%d inputs)", entry.getKey(), entry.getValue() )
			);
		}
		
		return sb.toString();
	}
	
	private static String tidyExtension( String extension ) {
		
		if (extension.isEmpty()) {
			return NO_EXTENSION;
		} else {
			return extension.toLowerCase();
		}		
	}
	
	private synchronized void incrCount( String extension ) {
		
		Integer cnt = map.get(extension);
		
		if (cnt==null) {
			map.put( extension, new Integer(1) );
		} else {
			cnt++;
		}
	}
}
