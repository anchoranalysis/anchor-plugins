package org.anchoranalysis.plugin.io.bean.rasterreader;

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


import java.util.List;

import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

class OpenedRasterAlterDimensions extends OpenedRaster {
	
	private OpenedRaster delegate;
	private ProcessDimensions processor;

	@FunctionalInterface
	public static interface ProcessDimensions {
		void maybeAlterDimensions( ImageDim sd ) throws RasterIOException;
	}
	
	public OpenedRasterAlterDimensions(OpenedRaster delegate, ProcessDimensions processor ) {
		super();
		this.delegate = delegate;
		this.processor = processor;
	}

	@Override
	public int numSeries() {
		return delegate.numSeries();
	}

	@Override
	public TimeSequence open(int seriesIndex,
			ProgressReporter progressReporter)
			throws RasterIOException {
		TimeSequence ts = delegate.open(seriesIndex, progressReporter);
		
		for( Stack stack : ts ) {
			processor.maybeAlterDimensions(stack.getDimensions());
		}
		
		return ts;
	}

	@Override
	public List<String> channelNames() {
		return delegate.channelNames();
	}

	@Override
	public int numChnl() throws RasterIOException {
		return delegate.numChnl();
	}

	@Override
	public ImageDim dim(int seriesIndex) throws RasterIOException {

		ImageDim sd = delegate.dim(seriesIndex);
		processor.maybeAlterDimensions(sd);
		return sd;
	}

	@Override
	public int numFrames() throws RasterIOException {
		return delegate.numFrames();
	}
	
	@Override
	public boolean isRGB() throws RasterIOException {
		return delegate.isRGB();
	}

	@Override
	public int bitDepth() throws RasterIOException {
		return delegate.bitDepth();
	}

	@Override
	public void close() throws RasterIOException {
		delegate.close();
		
	}
}