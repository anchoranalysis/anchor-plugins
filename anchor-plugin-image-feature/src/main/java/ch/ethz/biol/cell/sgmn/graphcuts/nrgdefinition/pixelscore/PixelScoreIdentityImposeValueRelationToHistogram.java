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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.feature.bean.pixelwise.score.PixelScore;
import org.anchoranalysis.image.feature.pixelwise.PixelwiseFeatureInitParams;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;

public class PixelScoreIdentityImposeValueRelationToHistogram extends PixelScore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int nrgChnlIndexCheck = 0;
	
	@BeanField
	private int nrgChnlIndexFail = 0;
	
	@BeanField
	private int histIndex = 0;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField
	private double value = 0;
	
	@BeanField
	private boolean max = true;		// We use the max, otherwise the min
	// END BEAN PROPERTIES
	
	private int histMax;
		
	@Override
	protected double calcCast(PixelScoreFeatureCalcParams params)
			throws FeatureCalcException {
		double pxlValue = params.getPxl(nrgChnlIndexCheck);
		
		if (relation.create().isRelationToValueTrue(pxlValue, histMax)) {
			return value;
		}
		return params.getPxl(nrgChnlIndexFail);
	}

	@Override
	public void beforeCalcCast(PixelwiseFeatureInitParams params, FeatureSessionCacheRetriever session) throws InitException {
		if (max) {
			histMax = params.getHist(histIndex).calcMax();
		} else {
			histMax = params.getHist(histIndex).calcMin();
		}
	}

	public int getHistIndex() {
		return histIndex;
	}

	public void setHistIndex(int histIndex) {
		this.histIndex = histIndex;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getNrgChnlIndexCheck() {
		return nrgChnlIndexCheck;
	}

	public void setNrgChnlIndexCheck(int nrgChnlIndexCheck) {
		this.nrgChnlIndexCheck = nrgChnlIndexCheck;
	}

	public int getNrgChnlIndexFail() {
		return nrgChnlIndexFail;
	}

	public void setNrgChnlIndexFail(int nrgChnlIndexFail) {
		this.nrgChnlIndexFail = nrgChnlIndexFail;
	}

	public boolean isMax() {
		return max;
	}

	public void setMax(boolean max) {
		this.max = max;
	}





}
