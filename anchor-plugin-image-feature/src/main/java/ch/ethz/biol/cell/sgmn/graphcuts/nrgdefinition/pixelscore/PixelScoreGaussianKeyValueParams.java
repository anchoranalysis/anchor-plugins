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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.plugin.operator.feature.score.GaussianScoreCalculator;

public class PixelScoreGaussianKeyValueParams extends PixelScoreParamsBase {

	// START BEAN PROPERTIES
	@BeanField
	private String keyMean;
	
	@BeanField
	private String keyStdDev;
	
	@BeanField
	private double shift;
	// END BEAN PROPERTIES
	
	private double mean;
	private double stdDev;

	@Override
	protected void setupParams(KeyValueParams kpv) throws InitException {
		mean = extractParamsAsDouble(kpv, keyMean);
		stdDev = extractParamsAsDouble(kpv, keyStdDev);
	}
	
	@Override
	protected double deriveScoreFromPixelVal(int pixelVal) {
		
		// Values higher than the mean should be included for definite
		if (pixelVal>mean) {
			return 1.0;
		}
		
		double scoreBeforeShift = GaussianScoreCalculator.calc(mean, stdDev, pixelVal, false, false);
		
		double scoreShifted = (scoreBeforeShift - shift) / (1-shift);
		
		return (scoreShifted/2) + 0.5;
	}
	
	public String getKeyMean() {
		return keyMean;
	}

	public void setKeyMean(String keyMean) {
		this.keyMean = keyMean;
	}

	public String getKeyStdDev() {
		return keyStdDev;
	}

	public void setKeyStdDev(String keyStdDev) {
		this.keyStdDev = keyStdDev;
	}

	public double getShift() {
		return shift;
	}

	public void setShift(double shift) {
		this.shift = shift;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}
}
