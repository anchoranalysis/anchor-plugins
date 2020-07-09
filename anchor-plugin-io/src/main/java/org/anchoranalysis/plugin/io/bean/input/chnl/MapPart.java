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


import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesConcatenate;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeriesMap;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.io.input.FileInput;

import lombok.RequiredArgsConstructor;

/**
 * Provides a set of channels as an input, each of which has a name.
 * 
 * <p>The standard implementation of {@link NamedChnlsInputPart}
 * 
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class MapPart extends NamedChnlsInputPart {

	// START REQUIRED ARGUMENTS
	private final FileInput delegate;
	private final RasterReader rasterReader;
	private final ImgChnlMapCreator chnlMapCreator;
	
	/** This is to correct for a problem with formats such as czi where the seriesIndex doesn't indicate
	 *   the total number of series but rather is incremented with each acquisition, so for our purposes
	 *   we treat it as if its 0 */
	private final boolean useLastSeriesIndexOnly;
	// END REQUIRED ARGUMENTS
	
	// We cache a certain amount of stacks read for particular series
	private OpenedRaster openedRasterMemo = null;
	private ImgChnlMap chnlMap = null;
	
	@Override
	public ImageDimensions dim( int seriesIndex ) throws RasterIOException {
		return openedRaster().dim(seriesIndex);
	}

	@Override
	public int numSeries() throws RasterIOException {
		if (useLastSeriesIndexOnly) {
			return 1;
		} else {
			return openedRaster().numSeries();
		}
	}
	
	@Override
	public boolean hasChnl( String chnlName ) throws RasterIOException {
		return chnlMap().keySet().contains(chnlName);
	}
	
	// Where most of our time is being taken up when opening a raster
	@Override
	public NamedChnlCollectionForSeries createChnlCollectionForSeries( int seriesNum, ProgressReporter progressReporter ) throws RasterIOException {

		// We always use the last one
		if (useLastSeriesIndexOnly) {
			seriesNum = openedRaster().numSeries()-1;
		}
		
		NamedChnlCollectionForSeriesConcatenate out = new NamedChnlCollectionForSeriesConcatenate();
		out.add(
			new NamedChnlCollectionForSeriesMap(
				openedRaster(),
				chnlMap(),
				seriesNum
			)
		);
		return out;
	}
	
	@Override
	public String descriptiveName() {
		return delegate.descriptiveName();
	}
	
	@Override
	public List<Path> pathForBindingForAllChannels() {
		ArrayList<Path> out = new ArrayList<>();
		pathForBinding().ifPresent(out::add);
		return out;
	}

	@Override
	public Optional<Path> pathForBinding() {
		return delegate.pathForBinding();
	}

	@Override
	public File getFile() {
		return delegate.getFile();
	}

	@Override
	public int numChnl() throws RasterIOException {
		return openedRaster().numChnl();
	}

	@Override
	public int bitDepth() throws RasterIOException {
		return openedRaster().bitDepth();
	}
	
	private ImgChnlMap chnlMap() throws RasterIOException {
		openedRaster();
		return chnlMap;
	}
	
	private OpenedRaster openedRaster() throws RasterIOException {
		if (openedRasterMemo==null) {
			openedRasterMemo = rasterReader.openFile(
				delegate.pathForBinding().orElseThrow( ()->
					new RasterIOException("A binding-path is needed in the delegate.")
				)
			);
			try {
				chnlMap = chnlMapCreator.createMap( openedRasterMemo );
			} catch (CreateException e) {
				throw new RasterIOException(e);
			}
		}
		return openedRasterMemo;
	}

	@Override
	public void close(ErrorReporter errorReporter) {
		
		if (openedRasterMemo!=null) {
			try {
				openedRasterMemo.close();
			} catch (RasterIOException e) {
				errorReporter.recordError(MapPart.class, e);
			}
		}
		delegate.close(errorReporter);
	}
}
