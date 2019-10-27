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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.GreaterThanEqualTo;
import org.anchoranalysis.core.relation.LessThan;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.feature.bean.pixelwise.score.PixelScore;
import org.anchoranalysis.image.feature.pixelwise.PixelwiseFeatureInitParams;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.histogram.Histogram;

// Same as PixelScoreDifference but calculates the width as the std deviation of the histogram
//  associated with the init params, and the level is calculated from the histogram
public class PixelScoreDifferenceCalculateLevelStdDev extends PixelScore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int nrgChnlIndex = 0;
	
	@BeanField
	private int indexHistogram = 0;

	@BeanField
	private int minDifference = 0;
	
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField
	private double widthFactor = 1.0;
	// END BEAN PROPERTIES

	private int level;
	private double widthLessThan;
	private double widthGreaterThan;
	
	@Override
	public void beforeCalcCast(PixelwiseFeatureInitParams params, FeatureSessionCacheRetriever session) throws InitException {
		super.beforeCalcCast(params, session);
		
		Histogram hist = params.getHist(indexHistogram);
		
		
		try {
			this.level = calculateLevel.calculateLevel(hist);
		} catch (OperationFailedException e) {
			throw new InitException(e);
		}
		
		Histogram lessThan = hist.threshold( new LessThan(), level );
		Histogram greaterThan = hist.threshold( new GreaterThanEqualTo(), level );
		
		this.widthLessThan = lessThan.stdDev() * widthFactor;
		this.widthGreaterThan = greaterThan.stdDev() * widthFactor;
	}
	
	@Override
	public double calcCast(PixelScoreFeatureCalcParams params)
			throws FeatureCalcException {
		return PixelScoreDifference.calcDiffFromValue(params.getPxl(nrgChnlIndex), level, widthGreaterThan, widthLessThan, minDifference);
	}

	public int getMinDifference() {
		return minDifference;
	}

	public void setMinDifference(int minDifference) {
		this.minDifference = minDifference;
	}

	public int getNrgChnlIndex() {
		return nrgChnlIndex;
	}

	public void setNrgChnlIndex(int nrgChnlIndex) {
		this.nrgChnlIndex = nrgChnlIndex;
	}

	public int getIndexHistogram() {
		return indexHistogram;
	}

	public void setIndexHistogram(int indexHistogram) {
		this.indexHistogram = indexHistogram;
	}

	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}

	public double getWidthFactor() {
		return widthFactor;
	}

	public void setWidthFactor(double widthFactor) {
		this.widthFactor = widthFactor;
	}
	
	
}
