package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.apache.commons.io.FilenameUtils;

/**
 * Copies files to a number 001 002 etc. in the same order they are inputted.
 * 
 * No shuffling occurs.
 * 
 * @author feehano
 *
 */
public class Anonymize extends CopyFilesNaming {

	/**
	 * 
	 */
	// START BEAN PROPERTIES
	/** Iff TRUE, a mapping.csv file is created showing the mapping between the original-names and the anonymized
	 *   versions */
	@BeanField
	private boolean outputCSV = true;
	// END BEAN PROPERTIES

	private static String OUTPUT_CSV_FILENAME = "output.csv";
	
	private static class FileMapping {
		
		private Path original;
		private String anonymized;
		private int iter;
		
		public FileMapping(Path original, String anonymized, int iter) {
			super();
			this.original = original;
			this.anonymized = anonymized;
			this.iter = iter;
		}

		public Path getOriginal() {
			return original;
		}

		public String getAnonymized() {
			return anonymized;
		}
		
		public int getIter() {
			return iter;
		}
	}
	
	private List<FileMapping> listMappings;
	private String formatStr;
	
	@Override
	public void beforeCopying(Path destDir, int totalNumFiles) {
		listMappings = outputCSV ? new ArrayList<>() : null;
		
		formatStr = createFormatStrForMaxNum(totalNumFiles);
	}
	

	@Override
	public Path destinationPathRelative(Path sourceDir, Path destDir, File file, int iter) throws AnchorIOException {
		String ext = FilenameUtils.getExtension(file.toString());
		String fileNameNew = createNumericString(iter) + "." + ext;
		
		synchronized(listMappings) {
			listMappings.add(
				new FileMapping(
					NamingUtilities.filePathDiff( sourceDir, file.toPath() ),
					fileNameNew,
					iter
				)
			);
		}
		return Paths.get(fileNameNew);
	}

	@Override
	public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {

		if (listMappings!=null && dummyMode==false) {
			writeOutputCSV(destDir);
			listMappings = null;
		}
	}
	
	private void writeOutputCSV(Path destDir) throws AnchorIOException {
		
		Path csvOut = destDir.resolve(OUTPUT_CSV_FILENAME);
		
		CSVWriter csvWriter = CSVWriter.create(csvOut);
		
		csvWriter.writeHeaders( Arrays.asList("iter","in","out") );
		
		try {
			for( FileMapping mapping : listMappings ) {
				csvWriter.writeRow(
					Arrays.asList(
						new TypedValue( mapping.getIter() ),
						new TypedValue( mapping.getOriginal().toString() ),
						new TypedValue( mapping.getAnonymized() )
					)
				);
			}
		} finally {
			csvWriter.close();
		}
	}
	
	private String createNumericString( int iter ) {
		return String.format(formatStr, iter);
	}
		
	private static String createFormatStrForMaxNum( int maxNum ) {
		int maxNumDigits = (int) Math.ceil(Math.log10(maxNum));
		
		if (maxNumDigits>0) {
			return "%0" + maxNumDigits + "d";
		} else {
			return "%d";
		}
	}

}
