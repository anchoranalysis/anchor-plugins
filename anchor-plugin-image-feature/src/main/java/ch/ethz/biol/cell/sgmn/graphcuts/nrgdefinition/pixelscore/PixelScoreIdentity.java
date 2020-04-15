package ch.ethz.biol.cell.sgmn.graphcuts.nrgdefinition.pixelscore;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreIdentity extends PixelScore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int nrgChnlIndex = 0;
	
	@BeanField
	private boolean normalize = true;
	
	@BeanField
	private double factorLow = 1.0;
	
	@BeanField
	private double factorHigh = 1.0;
	
	@BeanField
	private boolean keepExtremes = false;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(int[] pixelVals)	throws FeatureCalcException {
		
		double pxlValue = pixelVals[nrgChnlIndex];
		
		if (normalize) {
			double val = pxlValue/255;
			
			if (keepExtremes) {
				if (val==0.0) {
					return val;
				}
				if (val==1.0) {
					return val;
				}
			}
			
			if (val<0.5) {
				return 0.5 - ((0.5-val) * factorLow);
			} else if (val>0.5) {
				return 0.5 + ((val-0.5) * factorHigh); 
			} else {
				return val;
			}
			
		} else {
			return pxlValue;
		}
	}

	public int getNrgChnlIndex() {
		return nrgChnlIndex;
	}

	public void setNrgChnlIndex(int nrgChnlIndex) {
		this.nrgChnlIndex = nrgChnlIndex;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public double getFactorLow() {
		return factorLow;
	}

	public void setFactorLow(double factorLow) {
		this.factorLow = factorLow;
	}

	public double getFactorHigh() {
		return factorHigh;
	}

	public void setFactorHigh(double factorHigh) {
		this.factorHigh = factorHigh;
	}

	public boolean isKeepExtremes() {
		return keepExtremes;
	}

	public void setKeepExtremes(boolean keepExtremes) {
		this.keepExtremes = keepExtremes;
	}

	

}
