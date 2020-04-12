package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculation;

public class MaxOverlapRatio extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double max = -1;
	
	@BeanField
	private double penaltyValue = -10;
	
	@BeanField
	private boolean includeShell = false;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
	
	public MaxOverlapRatio() {
	}
	
	public MaxOverlapRatio(double maxOverlap) {
		this();
		this.max = maxOverlap;
	}
	
	@Override
	public String getParamDscr() {
		return String.format("max=%f", max );
	}

	@Override
	public double calc( CacheableParams<NRGElemPairCalcParams> paramsCacheable ) throws FeatureCalcException {
		
		NRGElemPairCalcParams params = paramsCacheable.getParams();
		
		try {
			double ratio = OverlapRatio.calcOverlapRatioMin(
				params.getObj1(),
				params.getObj2(),
				paramsCacheable.calc( new OverlapCalculation(regionID) ),
				regionID,
				false
			);
			
			if ( ratio > max ) {
				return penaltyValue;
			} else {
				return 0;
			}
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getPenaltyValue() {
		return penaltyValue;
	}

	public void setPenaltyValue(double penaltyValue) {
		this.penaltyValue = penaltyValue;
	}

	public boolean isIncludeShell() {
		return includeShell;
	}

	public void setIncludeShell(boolean includeShell) {
		this.includeShell = includeShell;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

}

