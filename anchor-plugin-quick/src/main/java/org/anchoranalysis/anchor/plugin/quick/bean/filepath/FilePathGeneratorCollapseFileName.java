package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.apache.commons.io.FilenameUtils;

/**
 * Takes a file-path of form  somedir/somename.ext  and converts to somedir.ext
 * @author FEEHANO
 *
 */
public class FilePathGeneratorCollapseFileName extends FilePathGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN FIELDS
	@BeanField
	private FilePathGenerator filePathGenerator;
	// END BEAN FIELDS
	
	@Override
	public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
		Path path = filePathGenerator.outFilePath(pathIn, debugMode);
		return collapse(path);
	}
	
	private static Path collapse( Path path ) throws AnchorIOException {
		
		PathTwoParts pathDir = new PathTwoParts(path);
		
		String ext = FilenameUtils.getExtension( pathDir.getSecond().toString() );
		if (ext.isEmpty()) {
			return pathDir.getFirst();
		}
		
		String pathNew = String.format("%s.%s", pathDir.getFirst().toString(), ext);
		
		return Paths.get(pathNew);
	}

	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}

}
