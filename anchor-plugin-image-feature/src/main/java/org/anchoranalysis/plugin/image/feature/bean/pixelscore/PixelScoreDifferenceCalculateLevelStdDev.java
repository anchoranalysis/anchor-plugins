package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

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
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.LessThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;

// Same as PixelScoreDifference but calculates the width as the std deviation of the histogram
//  associated with the init params, and the level is calculated from the histogram
public class PixelScoreDifferenceCalculateLevelStdDev extends PixelScoreCalculateLevelBase {

	// START BEAN PROPERTIES
	@BeanField
	private int minDifference = 0;
	
	@BeanField
	private double widthFactor = 1.0;
	// END BEAN PROPERTIES

	private double widthLessThan;
	private double widthGreaterThan;
	
	@Override
	protected void beforeCalcSetup(Histogram hist, int level) throws OperationFailedException {
			
		Histogram lessThan = hist.threshold(
			new RelationToConstant(
				new LessThanBean(),
				level
			)
		);
		Histogram greaterThan = hist.threshold(
			new RelationToConstant(
				new GreaterThanEqualToBean(),
				level
			)
		);
		
		this.widthLessThan = lessThan.stdDev() * widthFactor;
		this.widthGreaterThan = greaterThan.stdDev() * widthFactor;
	}

	@Override
	protected double calcForPixel(int pxlValue, int level) {
		return PixelScoreDifference.calcDiffFromValue(
			pxlValue,
			level,
			widthGreaterThan,
			widthLessThan,
			minDifference
		);
	}

	public int getMinDifference() {
		return minDifference;
	}

	public void setMinDifference(int minDifference) {
		this.minDifference = minDifference;
	}

	public double getWidthFactor() {
		return widthFactor;
	}

	public void setWidthFactor(double widthFactor) {
		this.widthFactor = widthFactor;
	}
}