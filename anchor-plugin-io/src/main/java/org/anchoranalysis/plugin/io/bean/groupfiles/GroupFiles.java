package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.io.File;

/*
 * #%L
 * anchor-image-io
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


import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.chnl.factory.ChnlFactorySingleType;
import org.anchoranalysis.image.chnl.factory.ChnlFactoryByte;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.io.params.InputContextParams;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.groupfiles.check.CheckParsedFilePathBag;
import org.anchoranalysis.plugin.io.bean.groupfiles.parser.FilePathParser;
import org.anchoranalysis.plugin.io.bean.input.file.Files;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;


/**
 * An input-manager that can group together files to form stacks or time-series based on
 *  finding patterns in the file path (via regular expressions)
 * 
 * The manager applies a regular expression on a set of input file paths, and identifies one or more groups:
 *   One group is the image key (something that uniquely identifies each image)
 *	 One group is the slice-identifier (identifies the z slice, must be positive integer)
 *   One group is the channel-identifier (identifies the channel, must be positive integer)
 *
 *   For each image key, an image is loaded using the slice and channel-identifiers. 
 *
 *	 Integer numbers are simply loaded in ascending numerical order. So gaps are allowed, and starting numbers are irrelevant.
 *
 *   It is more powerful than MultiFileReader, which expects only one image per folder. This class allows multiple images per folder
 *     and only performs a single glob for filenames
 * 
 * @author Owen Feehan
 *
 */
public class GroupFiles extends InputManager<NamedChnlsInput> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEANS
	@BeanField
	private Files fileInput;
	
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	
	@BeanField
	private FilePathParser filePathParser;
	
	@BeanField
	private boolean requireAllFilesMatch = false;
	
	@BeanField
	private ImgChnlMapCreator imgChnlMapCreator;
	
	@BeanField
	private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders(2);
	
	/**
	 * Imposes a condition on each parsedFilePathBag which must be-fulfilled if a file is to be included
	 */
	@BeanField @Optional
	private CheckParsedFilePathBag checkParsedFilePathBag;
	// END BEANS
	
	private Logger log = Logger.getLogger(GroupFiles.class.getName());
	
	static ChnlFactorySingleType imgChnlFactoryByte = new ChnlFactoryByte();
	
	@Override
	public List<NamedChnlsInput> inputObjects(
			InputContextParams inputContext, ProgressReporter progressReporter, LogErrorReporter logger)
			throws AnchorIOException {
		
		GroupFilesMap map = new GroupFilesMap();
	
		// Iterate through each file, match against the reg-exp and populate a hash-map
		Iterator<FileInput> itrFiles = fileInput.inputObjects(inputContext, progressReporter, logger).iterator();
		while( itrFiles.hasNext() ) {
			
			FileInput f = itrFiles.next();
			
			String path =  f.getFile().getAbsolutePath();
			path = path.replaceAll("\\\\", "/");

			if ( filePathParser.setPath(path) ) {
				FileDetails fd = new FileDetails(
					Paths.get(path),
					filePathParser.getChnlNum(),
					filePathParser.getZSliceNum(),
					filePathParser.getTimeIndex()
				);
				map.add(filePathParser.getKey(), fd);		
				log.finer( String.format("Parse SUCC Input file: %s could be parsed", path) );
			} else {
				if (requireAllFilesMatch) {
					throw new AnchorIOException( String.format("File %s did not match parser", path) );
				}
				log.finer( String.format("Parse FAIL Input file: %s couldn't be parsed", path) );	
			}
		}

		return listFromMap(map);
	}
	
	private List<NamedChnlsInput> listFromMap( GroupFilesMap map ) {
		
		List<File> files = new ArrayList<>();
		List<MultiFileReaderOpenedRaster> openedRasters = new ArrayList<>(); 
		
		// Process the hash-map by key
		for( String key : map.keySet() ) {
			ParsedFilePathBag bag = map.get( key );
			assert(bag!=null);
			
			// If we have a condition to check against
			if (checkParsedFilePathBag!=null) {
				if (!checkParsedFilePathBag.accept(bag)) {
					continue;
				}
			}
						
			MultiFileReaderOpenedRaster or = new MultiFileReaderOpenedRaster( rasterReader, bag );
			files.add( Paths.get(key).toFile() );
			openedRasters.add( or );
		}
				
		List<DescriptiveFile> descriptiveNames = descriptiveNameFromFile.descriptiveNamesFor(files, "InvalidName");
		return zipIntoGrouping(descriptiveNames, openedRasters);		
	}

	private List<NamedChnlsInput> zipIntoGrouping(List<DescriptiveFile> df, List<MultiFileReaderOpenedRaster> or) {
		
		Iterator<DescriptiveFile> it1 = df.iterator();
		Iterator<MultiFileReaderOpenedRaster> it2 = or.iterator();
		
		List<NamedChnlsInput> result = new ArrayList<>();
		while (it1.hasNext() && it2.hasNext()) {
			DescriptiveFile d = it1.next();
			result.add(
					new GroupingInput(d.getFile().toPath(), it2.next(),imgChnlMapCreator, d.getDescriptiveName())
			);
		}
		return result;
	}

	public FilePathParser getFilePathParser() {
		return filePathParser;
	}

	public void setFilePathParser(FilePathParser filePathParser) {
		this.filePathParser = filePathParser;
	}

	public boolean isRequireAllFilesMatch() {
		return requireAllFilesMatch;
	}

	public void setRequireAllFilesMatch(boolean requireAllFilesMatch) {
		this.requireAllFilesMatch = requireAllFilesMatch;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public Files getFileInput() {
		return fileInput;
	}

	public void setFileInput(Files fileInput) {
		this.fileInput = fileInput;
	}

	public ImgChnlMapCreator getImgChnlMapCreator() {
		return imgChnlMapCreator;
	}

	public void setImgChnlMapCreator(ImgChnlMapCreator imgChnlMapCreator) {
		this.imgChnlMapCreator = imgChnlMapCreator;
	}

	public DescriptiveNameFromFile getDescriptiveNameFromFile() {
		return descriptiveNameFromFile;
	}

	public void setDescriptiveNameFromFile(
			DescriptiveNameFromFile descriptiveNameFromFile) {
		this.descriptiveNameFromFile = descriptiveNameFromFile;
	}

	public CheckParsedFilePathBag getCheckParsedFilePathBag() {
		return checkParsedFilePathBag;
	}

	public void setCheckParsedFilePathBag(
			CheckParsedFilePathBag checkParsedFilePathBag) {
		this.checkParsedFilePathBag = checkParsedFilePathBag;
	}


}
