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
import org.anchoranalysis.image.feature.bean.pixelwise.score.PixelScore;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;

public class PixelScoreDifference extends PixelScore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int nrgChnlIndexFirst = 0;
	
	@BeanField
	private int nrgChnlIndexSecond = 0;
	
	@BeanField
	private double width = 10;
	
	@BeanField
	private int minDifference = 0;
	// END BEAN PROPERTIES
	
	
	public static double calcDiffFromValue( int valFirst, int valSecond, double widthGreaterThan, double widthLessThan, int minDifference ) {
		double diff = (double) (valFirst - valSecond - minDifference);
		
		if (diff < (-1*widthLessThan) ) {
			return 0.0;
		}
		
		if (diff > widthGreaterThan) {
			return 1.0;
		}
		
		if (diff<0) {
			return (diff/(widthLessThan*2)) + 0.5;
		} else {
			return (diff/(widthGreaterThan*2)) + 0.5;
		}
	}
	
	public static double calcDiffFromValue( int valFirst, int valSecond, double width, int minDifference ) {
		return calcDiffFromValue( valFirst, valSecond, width, width, minDifference );
	}

	public static double calcDiffFromParams( PixelScoreFeatureCalcParams params, int nrgChnlIndexFirst, int nrgChnlIndexSecond, double width, int minDifference ) {
		return calcDiffFromValue( params.getPxl(nrgChnlIndexFirst), params.getPxl(nrgChnlIndexSecond), width, minDifference );
	}
	
	@Override
	public double calcCast(PixelScoreFeatureCalcParams params)
			throws FeatureCalcException {
		return calcDiffFromParams(params, nrgChnlIndexFirst, nrgChnlIndexSecond, width, minDifference);
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public int getNrgChnlIndexFirst() {
		return nrgChnlIndexFirst;
	}

	public void setNrgChnlIndexFirst(int nrgChnlIndexFirst) {
		this.nrgChnlIndexFirst = nrgChnlIndexFirst;
	}

	public int getNrgChnlIndexSecond() {
		return nrgChnlIndexSecond;
	}

	public void setNrgChnlIndexSecond(int nrgChnlIndexSecond) {
		this.nrgChnlIndexSecond = nrgChnlIndexSecond;
	}

	public int getMinDifference() {
		return minDifference;
	}

	public void setMinDifference(int minDifference) {
		this.minDifference = minDifference;
	}


}
