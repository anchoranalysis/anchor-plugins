package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

/**
 * Takes the XY-resolution determined by rasterReaderInput. Partitions this into three ranges, based on two thresholds.
 * 
 *    {@literal LOW_RANGE  <= thresholdLow < MIDDLE_RANGE < thresholdHigh <= HIGH_RANGE}
 * 
 * Then selects the corresponding rasterReader for further reading
 * 
 * Assumes X resolution and Y resolution are the same.
 * 
 * @author Owen Feehan
 *
 */
public class ThreeWayBranchXYResolution extends RasterReader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private RasterReader rasterReaderInput;
	
	@BeanField
	private RasterReader rasterReaderLow;
	
	@BeanField
	private RasterReader rasterReaderMiddle;
	
	@BeanField
	private RasterReader rasterReaderHigh;
	
	@BeanField
	private double thresholdLow;
	
	@BeanField
	private double thresholdHigh;
	// END BEAN PROPERTIES
	
	@Override
	public OpenedRaster openFile(Path filepath) throws RasterIOException {
		
		OpenedRaster orInput = rasterReaderInput.openFile(filepath);
		
		ImageDim sd = orInput.dim(0);
		
		if (Math.abs(sd.getRes().getX()-sd.getRes().getY())>1e-12) {
			throw new RasterIOException(
				String.format("X-Res (%f) must be equal to Y-Res (%f).", sd.getRes().getX(), sd.getRes().getY())
			);
		}
		
		double xyRes = sd.getRes().getX();
		
		if (xyRes < thresholdLow) {
			return rasterReaderLow.openFile(filepath);
		} else if (xyRes < thresholdHigh) {
			return rasterReaderMiddle.openFile(filepath);
		} else {
			return rasterReaderHigh.openFile(filepath);
		}
	}

	public RasterReader getRasterReaderInput() {
		return rasterReaderInput;
	}

	public void setRasterReaderInput(RasterReader rasterReaderInput) {
		this.rasterReaderInput = rasterReaderInput;
	}

	public RasterReader getRasterReaderLow() {
		return rasterReaderLow;
	}

	public void setRasterReaderLow(RasterReader rasterReaderLow) {
		this.rasterReaderLow = rasterReaderLow;
	}

	public RasterReader getRasterReaderMiddle() {
		return rasterReaderMiddle;
	}

	public void setRasterReaderMiddle(RasterReader rasterReaderMiddle) {
		this.rasterReaderMiddle = rasterReaderMiddle;
	}

	public RasterReader getRasterReaderHigh() {
		return rasterReaderHigh;
	}

	public void setRasterReaderHigh(RasterReader rasterReaderHigh) {
		this.rasterReaderHigh = rasterReaderHigh;
	}

	public double getThresholdLow() {
		return thresholdLow;
	}

	public void setThresholdLow(double thresholdLow) {
		this.thresholdLow = thresholdLow;
	}

	public double getThresholdHigh() {
		return thresholdHigh;
	}

	public void setThresholdHigh(double thresholdHigh) {
		this.thresholdHigh = thresholdHigh;
	}



}
