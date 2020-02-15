package org.anchoranalysis.plugin.io.bean.groupfiles;

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


import java.nio.file.Path;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.bean.chnl.map.creator.ImgChnlMapCreator;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.io.bean.input.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;


class GroupingInput extends NamedChnlsInputAsStack {

	// The opened raster with multiple files
	private OpenedRaster openedRaster = null;
	
	// A virtual path uniquely representing this particular file 
	private Path virtualPath;

	private ImgChnlMapCreator chnlMapCreator; 
	
	private ImgChnlMap chnlMap = null;
	
	private String descriptiveName;

	// The root object that is used to provide the descriptiveName and pathForBinding
	//
	public GroupingInput( Path virtualPath, MultiFileReaderOpenedRaster mfor, ImgChnlMapCreator chnlMapCreator, DescriptiveNameFromFile descriptiveNameFromFile ) {
		super();
		assert(virtualPath!=null);
		this.virtualPath = virtualPath;
		this.openedRaster = mfor;
		this.chnlMapCreator = chnlMapCreator;
		this.descriptiveName = descriptiveNameFromFile.createDescriptiveNameOrElse(
			virtualPath.toFile(),
			0,
			"InvalidName"
		);
	}

	@Override
	public int numSeries() throws RasterIOException {
		return openedRaster.getNumSeries();
	}

	@Override
	public ImageDim dim(int seriesIndex) throws RasterIOException {
		return openedRaster.dim(seriesIndex);
	}
	
	@Override
	public NamedChnlCollectionForSeries createChnlCollectionForSeries( int seriesNum, ProgressReporter progressReporter ) throws RasterIOException {
		
		// Lazy creation
		if (chnlMap==null) {
			try {
				chnlMap = chnlMapCreator.createMap(openedRaster);
			} catch (CreateException e) {
				throw new RasterIOException(e);
			}
		}
		return new NamedChnlCollectionForSeriesMap( openedRaster, chnlMap, seriesNum);
	}
	

	
	@Override
	public String descriptiveName() {
		return descriptiveName;
	}

	@Override
	public Path pathForBinding() {
		return virtualPath;
	}

	@Override
	public void close(ErrorReporter errorReporter) {
		try {
			openedRaster.close();
		} catch (RasterIOException e) {
			errorReporter.recordError(GroupingInput.class, e);
		}
	}
}
