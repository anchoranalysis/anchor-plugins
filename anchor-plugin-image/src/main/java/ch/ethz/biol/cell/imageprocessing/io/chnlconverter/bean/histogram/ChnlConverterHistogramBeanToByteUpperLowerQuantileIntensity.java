package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean.histogram;

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
import org.anchoranalysis.image.bean.chnl.converter.histogram.ChnlConverterHistogramBean;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.ChnlConverterAttached;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.histogram.ChnlConverterHistogramUpperLowerQuantileIntensity;

public class ChnlConverterHistogramBeanToByteUpperLowerQuantileIntensity extends ChnlConverterHistogramBean {

	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double quantileLower = 0.0;
	
	@BeanField
	private double quantileUpper = 1.0;

	/** Sets the min by multiplying the quantileLower by this constant */
	@BeanField
	private double scaleLower = 1.0;
	
	/** Sets the max by multiplying the quantileUpper by this constant */
	@BeanField
	private double scaleUpper = 1.0;
	// END BEAN PROPERTIES
	
	@Override
	public ChnlConverterAttached<Histogram, ?> createConverter() {
		return new ChnlConverterHistogramUpperLowerQuantileIntensity(quantileLower, quantileUpper, scaleLower, scaleUpper);
	}

	public double getQuantileLower() {
		return quantileLower;
	}

	public void setQuantileLower(double quantileLower) {
		this.quantileLower = quantileLower;
	}

	public double getQuantileUpper() {
		return quantileUpper;
	}

	public void setQuantileUpper(double quantileUpper) {
		this.quantileUpper = quantileUpper;
	}

	public double getScaleLower() {
		return scaleLower;
	}

	public void setScaleLower(double scaleLower) {
		this.scaleLower = scaleLower;
	}

	public double getScaleUpper() {
		return scaleUpper;
	}

	public void setScaleUpper(double scaleUpper) {
		this.scaleUpper = scaleUpper;
	}
		

}
