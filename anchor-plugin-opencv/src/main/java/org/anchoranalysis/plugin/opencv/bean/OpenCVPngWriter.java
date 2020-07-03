package org.anchoranalysis.plugin.opencv.bean;

/*-
 * #%L
 * anchor-plugin-opencv
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.ImgStackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVPngWriter extends RasterWriter {

	static {
		CVInit.alwaysExecuteBeforeCallingLibrary();
	}
	
	@Override
	public String dfltExt() {
		return "png";
	}

	@Override
	public void writeTimeSeriesStackByte(ImgStackSeries stackSeries, Path filePath, boolean makeRGB) throws RasterIOException {
		throw new RasterIOException("Writing time-series is unsupported for this format");
	}

	@Override
	public synchronized void writeStackByte(Stack stack, Path filePath, boolean makeRGB) throws RasterIOException {
		
		if(stack.getNumChnl()==3 && !makeRGB) {
			throw new RasterIOException("3-channel images can only be created as RGB");
		}
		
		try {
			Imgcodecs.imwrite(
				filePath.toString(),
				MatConverter.fromStack(stack)
			);
		} catch (CreateException e) {
			throw new RasterIOException(e);
		}
	}

	@Override
	public void writeStackShort(Stack stack, Path filePath, boolean makeRGB) throws RasterIOException {
		writeStackByte(stack, filePath, makeRGB);
	}

}
