package org.anchoranalysis.plugin.io.bean.filepath.rslvr;

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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

class HelperFilePathRslvr {

	private static Logger logger = Logger.getLogger( FilePathRslvrRegEx.class.toString() );
	
	public static FilePathPrefix createRootFolderPrefix( String path, String experimentIdentifier ) {
		String folder = new String( path + File.separator + experimentIdentifier + File.separator );
		return new FilePathPrefix( Paths.get(folder) );
	}
	
	
	/**
	 * Creates a FilePathPrefix based upon an ordered array of string components
	 * 
	 * @param outFolderPath the base folder of the prefixer
	 * @param components ordered array of string components
	 * @param fileAsFolder if TRUE, creates new sub-folders based upon matching groups. if FALSE, they are appended to the file-name instead
	 * @return
	 */
	public static FilePathPrefix createPrefix( Path outFolderPath, String[] components, boolean fileAsFolder ) {
		
		Path prefixPath = outFolderPath;
		
		if (fileAsFolder) {
			for (int g=0; g<components.length; g++) {
				prefixPath = prefixPath.resolve( components[g] );
			}
		}
		
		
		FilePathPrefix fpp = new FilePathPrefix( prefixPath );
		
		if (!fileAsFolder) {
			
			StringBuilder sb = new StringBuilder();
			for (int g=0; g<components.length; g++) {
				sb.append( components[g] );
				
				sb.append("_");
			}
			
			fpp.setFilenamePrefix( sb.toString() );
		}
		
		logger.log( Level.DEBUG, String.format("outFolderPathPrefix %s", prefixPath ) );
		return fpp;
	}
	
}
