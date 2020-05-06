package org.anchoranalysis.plugin.io.bean.input.chnl;

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


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.cache.CachedOperation;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.OperationOutFilePath;


public class NamedChnlsAppend extends NamedChnlsBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private InputManager<NamedChnlsInputPart> input;
	
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	
	@BeanField @OptionalBean
	private List<NamedBean<FilePathGenerator>> listAppend;
	
	@BeanField
	private boolean forceEagerEvaluation = false;
	
	@BeanField
	private boolean ignoreFileNotFoundAppend = false;
	
	@BeanField
	private boolean skipMissingChannels = false;
	// END BEAN PROPERTIES
	
	@Override
	public List<NamedChnlsInputPart> inputObjects(InputManagerParams params)
			throws AnchorIOException {

		try( ProgressReporterMultiple prm = new ProgressReporterMultiple(params.getProgressReporter(), 2)) {
			
			Iterator<NamedChnlsInputPart> itr = input.inputObjects(params).iterator();
			
			prm.incrWorker();
			
			List<NamedChnlsInputPart> listTemp = new ArrayList<>();
			while( itr.hasNext() ) {
				listTemp.add( itr.next() );
			}
			
			List<NamedChnlsInputPart> outList = createOutList(
				listTemp,
				new ProgressReporterOneOfMany(prm),
				params.isDebugModeActivated()
			);
			
			prm.incrWorker();
			
			return outList;
		}
	}
	
	private List<NamedChnlsInputPart> createOutList( List<NamedChnlsInputPart> listTemp, ProgressReporter progressReporter, boolean debugMode ) throws AnchorIOException {

		progressReporter.setMin(0);
		progressReporter.setMax( listTemp.size() );
		progressReporter.open();
		
		try {
		
			List<NamedChnlsInputPart> outList = new ArrayList<>();
			for( int i=0; i<listTemp.size(); i++) {
				
				NamedChnlsInputPart ncc = listTemp.get(i);
				
				if (ignoreFileNotFoundAppend) {
					
					try {
						outList.add( append(ncc, debugMode) );		
					} catch ( AnchorIOException e) {
						
					}
					
				} else {
					outList.add( append(ncc, debugMode) );	
				}
				
				progressReporter.update(i);
			}
			return outList;
			
		} finally {
			progressReporter.close();
		}
		
	}
	
	// We assume all the input files are single channel images
	private NamedChnlsInputPart append( final NamedChnlsInputPart ncc, boolean debugMode ) throws AnchorIOException {
		
		NamedChnlsInputPart out = ncc; 
		
		if (listAppend==null) {
			return out;
		}
		
		for( final NamedBean<FilePathGenerator> ni : listAppend) {
			
			// Delayed-calculation of the appending path as it can be a bit expensive when multiplied by so many items
			CachedOperation<Path,AnchorIOException> outPath = new OperationOutFilePath(
				ni,
				()->ncc.pathForBinding(),
				debugMode
			);
			
			if (forceEagerEvaluation) {
				Path path = outPath.doOperation();
				if (!Files.exists(path)) {
					
					if (skipMissingChannels) {
						continue;
					} else {
						throw new AnchorIOException( String.format("Append path: %s does not exist",path) );
					}
				}
			}
			
			out = new AppendPart<>(
				out,
				ni.getName(),
				0,
				outPath,
				rasterReader
			);
		}
	
		return out;
	}

	public InputManager<NamedChnlsInputPart> getInput() {
		return input;
	}

	public void setInput(InputManager<NamedChnlsInputPart> input) {
		this.input = input;
	}

	public List<NamedBean<FilePathGenerator>> getListAppend() {
		return listAppend;
	}

	public void setListAppend(List<NamedBean<FilePathGenerator>> listAppend) {
		this.listAppend = listAppend;
	}

	public boolean isForceEagerEvaluation() {
		return forceEagerEvaluation;
	}

	public void setForceEagerEvaluation(boolean forceEagerEvaluation) {
		this.forceEagerEvaluation = forceEagerEvaluation;
	}

	public boolean isIgnoreFileNotFoundAppend() {
		return ignoreFileNotFoundAppend;
	}

	public void setIgnoreFileNotFoundAppend(boolean ignoreFileNotFoundAppend) {
		this.ignoreFileNotFoundAppend = ignoreFileNotFoundAppend;
	}

	public boolean isSkipMissingChannels() {
		return skipMissingChannels;
	}

	public void setSkipMissingChannels(boolean skipMissingChannels) {
		this.skipMissingChannels = skipMissingChannels;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}
}
