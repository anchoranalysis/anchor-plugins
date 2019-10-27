package ch.ethz.biol.cell.sgmn.scale;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ScaleCalculatorMinXYRes extends ScaleCalculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private double minXYRes = 10e-9;
	// STOP BEAN PROPERTIES
	
	@Override
	public ScaleFactor calc(ImageDim srcDim) throws OperationFailedException {

		// If there is no resolution information we cannot scale
		if (srcDim.getRes().getX()==0 || srcDim.getRes().getY()==0) {
			throw new OperationFailedException("Channel has zero x or y resolution. Cannot scale to min res.");
		}
		
		int x = ScaleUtilities.calcRatio( srcDim.getRes().getX(), minXYRes );
		int y = ScaleUtilities.calcRatio( srcDim.getRes().getY(), minXYRes );
		
		if (x<0) {
			throw new OperationFailedException( String.format("Insufficient resolution (%E). %E is required", srcDim.getRes().getX(), minXYRes) ); 
		}
		
		if (y<0) {
			throw new OperationFailedException( String.format("Insufficient resolution (%E). %E is required", srcDim.getRes().getY(), minXYRes) ); 
		}

		double xScaleDownRatio = twoToMinusPower(x);
		double yScaleDownRatio = twoToMinusPower(y);
		
		getLogger().getLogReporter().logFormatted("Downscaling by factor %d,%d (mult by %f,%f)", x, y, xScaleDownRatio,  yScaleDownRatio);
		
		return new ScaleFactor(xScaleDownRatio, yScaleDownRatio);
	}
	
	private static double twoToMinusPower( int power ) {
		return Math.pow(2.0, -1.0 * power ); 
	}

	public double getMinXYRes() {
		return minXYRes;
	}

	public void setMinXYRes(double minXYRes) {
		this.minXYRes = minXYRes;
	}
}
