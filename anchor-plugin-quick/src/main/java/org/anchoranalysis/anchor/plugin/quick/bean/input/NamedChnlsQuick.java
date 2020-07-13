package org.anchoranalysis.anchor.plugin.quick.bean.input;

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

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.AppendStack;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorReplace;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnlsAppend;
import org.anchoranalysis.plugin.io.bean.input.chnl.NamedChnlsBase;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;

/**
 * A particular type of NamedChnls which allows easier input
 * of data, making several assumptions.
 *  
 * This is a convenient helper class to avoid a more complicated structure
 */
public class NamedChnlsQuick extends NamedChnlsBase {

	// START BEAN PROPERTIES
	/** A path to the main channel of each file */
	@BeanField
	private FileProviderWithDirectory fileProvider;
	
	@BeanField
	private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders();
	
	/** 
	 * This needs to be set if there is at least one adjacentChnl
	 * 
	 * This should be a regex (with a single group that is replaced) that is searched for in the path returned by fileProvider
	 * This only needs to be set if at least one adjacentChnl is specified
	 */
	@BeanField @AllowEmpty
	private String regexAdjacent = "";
	
	/** This should be a regex that is searched for in the path returned by fileProvider and returns
	 * two groups, the first  */
	
	/** 
	 * This needs to be set if there is at least one appendChnl
	 * 
	 * A regular-expression applied to the image file-path that matches three groups.
	 * The first group should correspond to top-level folder for the project
	 * The second group should correspond to the unique name of the dataset.
	 * The third group should correspond to the unique name of the experiment.
	 * */
	@BeanField @AllowEmpty
	private String regexAppend = "";
	
	/** The name of the channel provided by the rasters in file Provider */
	@BeanField
	private String mainChnlName;
	
	/** Index of the mainChnl */
	@BeanField
	private int mainChnlIndex = 0;
	
	/** Additional channels other than the main one, which are located in the main raster file */
	@BeanField
	private List<ImgChnlMapEntry> additionalChnls = new ArrayList<>();
	
	/** Channels that are located in a separate raster file adjacent to the main raster file */
	@BeanField
	private List<AdjacentFile> adjacentChnls = new ArrayList<>();
	
	/** Channels that are located in a separate raster file somewhere else in the project's structure */
	@BeanField
	private List<AppendStack> appendChnls = new ArrayList<>();
	
	/** If non-empty then a rooted file-system is used with this root */
	@BeanField @AllowEmpty
	private String rootName = "";
	
	/**
	 * If set, a CSV is read with two columns: the names of images and a
	 */
	@BeanField @OptionalBean
	private MatchedAppendCsv filterFilesCsv;
	
	/**
	 * The raster-reader to use for opening the main image
	 */
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
		
	/**
	 * The raster-reader to use for opening any appended-channels
	 */
	@BeanField @DefaultInstance
	private RasterReader rasterReaderAppend;
	
	/**
	 * The raster-reader to use for opening any adjacent-channels
	 */
	@BeanField @DefaultInstance
	private RasterReader rasterReaderAdjacent;
	// END BEAN PROPERTIES

	private InputManager<NamedChnlsInputPart> append;
	
	private BeanInstanceMap defaultInstances;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
		
		checkChnls( adjacentChnls, regexAdjacent, "adjacentChnl" );
		checkChnls( appendChnls, regexAppend, "appendChnl" );
	}
	
	/**
	 * Checks that if a list has an item, then a string must be non-empty
	 * 
	 * @param list
	 * @param mustBeNonEmpty
	 * @param errorText
	 * @throws BeanMisconfiguredException
	 */
	private static void checkChnls( List<?> list, String mustBeNonEmpty, String errorText ) throws BeanMisconfiguredException {
		if (!list.isEmpty() && mustBeNonEmpty.isEmpty()) {
			throw new BeanMisconfiguredException(
				String.format("If an %s is specified, regex must also be specified", errorText)
			);
		}
	}
	
	@Override
	public List<NamedChnlsInputPart> inputObjects(InputManagerParams params) throws AnchorIOException {
		createAppendedChnlsIfNecessary();
		return append.inputObjects(params);
	}
	
	private void createAppendedChnlsIfNecessary() throws AnchorIOException {
		if (this.append==null) {
			try {
				this.append = createAppendedChnls();
				append.checkMisconfigured(defaultInstances);
			} catch (BeanMisconfiguredException e) {
				throw new AnchorIOException("defaultInstances bean is misconfigured", e);
			}
		}
	}
	
	private InputManager<NamedChnlsInputPart> createAppendedChnls() throws BeanMisconfiguredException {
		
		InputManager<FileInput> files = InputManagerFactory.createFiles(
			rootName,
			fileProvider,
			descriptiveNameFromFile,
			regexAppend,
			filterFilesCsv
		);
		
		InputManager<NamedChnlsInputPart> chnls = NamedChnlsCreator.create(
			files,
			mainChnlName,
			mainChnlIndex,
			additionalChnls,
			rasterReader
		);
		
		chnls = appendChnls(
			chnls,
			createFilePathGeneratorsAdjacent(),
			rasterReaderAdjacent
		);
		
		chnls = appendChnls(
			chnls,
			createFilePathGeneratorsAppend(),
			rasterReaderAppend
		);
		
		return chnls;
	}
	
	private static NamedChnlsAppend appendChnls(
		InputManager<NamedChnlsInputPart> input,
		List<NamedBean<FilePathGenerator>> filePathGenerators,
		RasterReader rasterReader
	) {
		NamedChnlsAppend append = new NamedChnlsAppend();
		append.setIgnoreFileNotFoundAppend(false);
		append.setForceEagerEvaluation(false);
		append.setInput(input);
		append.setListAppend( filePathGenerators );
		append.setRasterReader(rasterReader);
		return append;		
	}
	
	private List<NamedBean<FilePathGenerator>> createFilePathGeneratorsAdjacent() {
		return FunctionalList.mapToList(adjacentChnls,	this::convertAdjacentFile);
	}
	
	private List<NamedBean<FilePathGenerator>> createFilePathGeneratorsAppend() throws BeanMisconfiguredException {
		
		List<NamedBean<FilePathGenerator>> out = new ArrayList<>();
		
		for( AppendStack stack : appendChnls) {
			try {
				out.add(
					convertAppendStack(stack)
				);
			} catch (BeanMisconfiguredException e) {
				throw new BeanMisconfiguredException(
					String.format(
						"Cannot create file-path-generator for %s and regex %s",
						stack.getName(),
						regexAppend
					),
					e
				);
			}
		}
		
		return out;
	}
	
	private NamedBean<FilePathGenerator> convertAdjacentFile( AdjacentFile file ) {
		
		FilePathGeneratorReplace fpg = new FilePathGeneratorReplace();
		fpg.setRegex(regexAdjacent);
		fpg.setReplacement( file.getReplacement() );
		return new NamedBean<>( file.getName(), fpg );
	}
	
	private NamedBean<FilePathGenerator> convertAppendStack( AppendStack stack ) throws BeanMisconfiguredException {
		return stack.createFilePathGenerator(rootName, regexAppend);
	}
		
	public FileProviderWithDirectory getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProviderWithDirectory fileProvider) {
		this.fileProvider = fileProvider;
	}

	public DescriptiveNameFromFile getDescriptiveNameFromFile() {
		return descriptiveNameFromFile;
	}

	public void setDescriptiveNameFromFile(DescriptiveNameFromFile descriptiveNameFromFile) {
		this.descriptiveNameFromFile = descriptiveNameFromFile;
	}
	
	public String getMainChnlName() {
		return mainChnlName;
	}

	public void setMainChnlName(String mainChnlName) {
		this.mainChnlName = mainChnlName;
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public List<AdjacentFile> getAdjacentChnls() {
		return adjacentChnls;
	}

	public void setAdjacentChnls(List<AdjacentFile> adjacentChnls) {
		this.adjacentChnls = adjacentChnls;
	}

	public List<ImgChnlMapEntry> getAdditionalChnls() {
		return additionalChnls;
	}

	public void setAdditionalChnls(List<ImgChnlMapEntry> additionalChnls) {
		this.additionalChnls = additionalChnls;
	}

	public int getMainChnlIndex() {
		return mainChnlIndex;
	}

	public void setMainChnlIndex(int mainChnlIndex) {
		this.mainChnlIndex = mainChnlIndex;
	}

	public String getRegexAdjacent() {
		return regexAdjacent;
	}

	public void setRegexAdjacent(String regexAdjacent) {
		this.regexAdjacent = regexAdjacent;
	}

	public String getRegexAppend() {
		return regexAppend;
	}

	public void setRegexAppend(String regexAppend) {
		this.regexAppend = regexAppend;
	}

	public List<AppendStack> getAppendChnls() {
		return appendChnls;
	}

	public void setAppendChnls(List<AppendStack> appendChnls) {
		this.appendChnls = appendChnls;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public RasterReader getRasterReaderAppend() {
		return rasterReaderAppend;
	}

	public void setRasterReaderAppend(RasterReader rasterReaderAppend) {
		this.rasterReaderAppend = rasterReaderAppend;
	}

	public RasterReader getRasterReaderAdjacent() {
		return rasterReaderAdjacent;
	}

	public void setRasterReaderAdjacent(RasterReader rasterReaderAdjacent) {
		this.rasterReaderAdjacent = rasterReaderAdjacent;
	}
}
