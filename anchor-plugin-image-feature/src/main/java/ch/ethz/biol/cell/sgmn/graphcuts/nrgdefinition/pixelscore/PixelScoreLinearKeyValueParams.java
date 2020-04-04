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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.pixelwise.score.PixelScore;
import org.anchoranalysis.image.feature.pixelwise.PixelwiseFeatureInitParams;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;

import ch.ethz.biol.cell.mpp.nrg.feature.operator.LinearScore;

public class PixelScoreLinearKeyValueParams extends PixelScore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String keyMin;
	
	@BeanField
	private String keyMax;
	
	@BeanField
	private int nrgChnlIndex = 0;
	// END BEAN PROPERTIES
	
	private double min;
	private double max;
	
	@Override
	protected double calcCast(CacheableParams<PixelScoreFeatureCalcParams> paramsCacheable)
			throws FeatureCalcException {
		
		PixelScoreFeatureCalcParams params = paramsCacheable.getParams();
		
		double val = params.getPxl(nrgChnlIndex);
		double score = (LinearScore.calc( val, min, max )/2) + 0.5;
		
		if (score<0) {
			score = 0;
		}
		
		if (score>1) {
			score = 1;
		}
		
		return score;
	}
	
	@Override
	public void beforeCalcCast(PixelwiseFeatureInitParams params, FeatureSessionCacheRetriever session) throws InitException {
		
		super.beforeCalcCast(params, session);
		
		KeyValueParams kpv = params.getKeyValueParams(); 

		if (!kpv.containsKey(keyMin)) {
			throw new InitException( String.format("Key '%s' does not exist",keyMin));
		}
		
		if (!kpv.containsKey(keyMax)) {
			throw new InitException( String.format("Key '%s' does not exist",keyMax));
		}
		
		min = Double.valueOf( kpv.getProperty(keyMin) );
		max = Double.valueOf( kpv.getProperty(keyMax) );
	}

	public int getNrgChnlIndex() {
		return nrgChnlIndex;
	}

	public void setNrgChnlIndex(int nrgChnlIndex) {
		this.nrgChnlIndex = nrgChnlIndex;
	}

	public String getKeyMin() {
		return keyMin;
	}

	public void setKeyMin(String keyMin) {
		this.keyMin = keyMin;
	}

	public String getKeyMax() {
		return keyMax;
	}

	public void setKeyMax(String keyMax) {
		this.keyMax = keyMax;
	}

}
