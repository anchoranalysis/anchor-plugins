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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.FilePathBaseAppendToManager;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.descriptivename.LastFolders;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.params.InputContextParams;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManagerBase;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.input.stack.Stacks;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;

/**
 * A quicker for of multi-input manager that makes various assumptions
 *
 */
public class MultiInputManagerQuick extends MultiInputManagerBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** If non-empty then a rooted file-system is used with this root */	
	@BeanField @AllowEmpty
	private String rootName = "";
	
	@BeanField
	private FileProviderWithDirectory fileProvider;
	
	@BeanField
	private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders();
	
	@BeanField
	private String inputName;
	
	/*** Additional entities that are appended to the multi-input */
	@BeanField
	private List<FilePathBaseAppendToManager> listAppend = new ArrayList<>();
	
	/** A regular-expression applied to the image file-path that matches three groups.
	 * The first group should correspond to the unique name of the top-level owner
	 * The second group should correspond to the unique name of the dataset.
	 * The third group should correspond to the unique name of the experiment.
	 * */
	@BeanField @Optional
	private String regex;
	
	/** Additional channels other than the main one, which are located in the main raster file
	 * 
	 *  If this list has at least one, then we treat the main raster file not as a stack, but break
	 *    it into separate channels that are each presented as a separate stack to the MultiInput 
	 * */
	@BeanField
	private List<ImgChnlMapEntry> additionalChnls = new ArrayList<ImgChnlMapEntry>();
	
	/**
	 * If set, a CSV is read with two columns: the names of images and a
	 */
	@BeanField @Optional
	private MatchedAppendCsv filterFilesCsv;
	
	/**
	 * If true, a raster-stack is treated as a single-channel, even if multiple exist (and no additionalChnl is set)
	 */
	@BeanField
	private boolean stackAsChnl = false;
	
	/**
	 * If either stackAsChnl==TRUE or we have specified additionalChnls this indicated which channel to use
	 * from the stack
	 */
	@BeanField
	private int chnlIndex = 0;
	
	/**
	 * The raster-reader for reading the main file
	 */
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	
	/**
	 * The raster-reader for reading any appended files
	 */
	@BeanField @DefaultInstance
	private RasterReader rasterReaderAppend;
	// END BEAN PROPERTIES
	
	private MultiInputManager inputManager;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		
		this.inputManager = createMulti();
		inputManager.checkMisconfigured(defaultInstances);
		
		if (additionalChnls.size()>0 && regex==null) {
			throw new BeanMisconfiguredException("If there is at least one additionalChnl then regex must be set");
		}
	}
	
	private MultiInputManager createMulti() throws BeanMisconfiguredException {
		MultiInputManager inputManager = new MultiInputManager();
		inputManager.setInputName(inputName);
		inputManager.setInput( createStacks() );
		inputManager.setRasterReader(rasterReaderAppend);
		
		// Add all the various types of items that can be appended
		for( FilePathBaseAppendToManager append : listAppend) {
			append.addToManager(inputManager, rootName, regex);
		}
		
		return inputManager;
	}
	
	private InputManager<? extends ProvidesStackInput> createStacks() throws BeanMisconfiguredException {
		InputManager<FileInput> files = InputManagerFactory.createFiles(
			rootName,
			fileProvider,
			descriptiveNameFromFile,
			regex,
			filterFilesCsv
		);
		
		if (stackAsChnl || additionalChnls.size()>0) {
			// Then we treat the main raster as comprising of multiple independent channels
			//  and each are presented separately to the MultiInput as stacks
			//
			// Channel 0 always takes the inputName
			// The other channels are defined by the contents of the ImgChnlMapEntry
			return NamedChnlsCreator.create(
				files,
				inputName,
				chnlIndex,
				additionalChnls,
				rasterReader
			);
			
		} else {
			// Normal mode, where we simply wrap the FileProvider in a Stacks
			Stacks stacks = new Stacks(files);
			stacks.setRasterReader(rasterReader);
			return stacks;
		}
	}
	
	
	@Override
	public List<MultiInput> inputObjects(InputContextParams inputContext, ProgressReporter progressReporter)
			throws IOException, DeserializationFailedException {
		return inputManager.inputObjects(inputContext, progressReporter);
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
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

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public List<FilePathBaseAppendToManager> getListAppend() {
		return listAppend;
	}

	public void setListAppend(List<FilePathBaseAppendToManager> listAppend) {
		this.listAppend = listAppend;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public List<ImgChnlMapEntry> getAdditionalChnls() {
		return additionalChnls;
	}

	public void setAdditionalChnls(List<ImgChnlMapEntry> additionalChnls) {
		this.additionalChnls = additionalChnls;
	}

	public MatchedAppendCsv getFilterFilesCsv() {
		return filterFilesCsv;
	}

	public void setFilterFilesCsv(MatchedAppendCsv filterFilesCsv) {
		this.filterFilesCsv = filterFilesCsv;
	}

	public boolean isStackAsChnl() {
		return stackAsChnl;
	}

	public void setStackAsChnl(boolean stackAsChnl) {
		this.stackAsChnl = stackAsChnl;
	}

	public int getChnlIndex() {
		return chnlIndex;
	}

	public void setChnlIndex(int chnlIndex) {
		this.chnlIndex = chnlIndex;
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


}
