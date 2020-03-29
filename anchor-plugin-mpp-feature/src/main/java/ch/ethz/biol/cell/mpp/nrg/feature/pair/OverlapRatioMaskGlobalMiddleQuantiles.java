package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobalMiddleQuantiles;

public class OverlapRatioMaskGlobalMiddleQuantiles extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private boolean useMax = false;
	
	@BeanField
	private double quantileLow = 0.0;
	
	@BeanField
	private double quantileHigh = 1.0;
	// END BEAN PROPERTIES
	
	private RelationBean relationToThreshold = new EqualToBean();
	
	private CachedCalculation<Double> cc;
	
	public OverlapRatioMaskGlobalMiddleQuantiles() {
	}
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		
		cc = cache.search( new OverlapCalculationMaskGlobalMiddleQuantiles(regionID, nrgIndex, (byte) maskValue, quantileLow, quantileHigh) );
	}
		
	@Override
	public double calcCast( NRGElemPairCalcParams params ) throws FeatureCalcException {
		
		assert( cc!=null );
		 
		try {
			return calcOverlapRatioMin( params.getObj1(), params.getObj2(), cc.getOrCalculate(params), regionID, false );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}
	
	public static double calcMinVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationToValue relationToThreshold,
		int nrgIndex,
		int maskValue
	) throws FeatureCalcException {
		try {
			VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			
			long size1 = pxlStats1.countThreshold(relationToThreshold, maskValue);
			long size2 = pxlStats2.countThreshold(relationToThreshold, maskValue);
			return Math.min( size1, size2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public static double calcMaxVolume(
			PxlMarkMemo obj1,
			PxlMarkMemo obj2,
			int regionID,
			RelationToValue relationToThreshold,
			int nrgIndex,
			int maskValue
		) throws FeatureCalcException {
			try {
				VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(nrgIndex, regionID);
				VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(nrgIndex, regionID);
				
				long size1 = pxlStats1.countThreshold(relationToThreshold, maskValue);
				long size2 = pxlStats2.countThreshold(relationToThreshold, maskValue);
				return Math.max( size1, size2 );
			} catch (ExecuteException e) {
				throw new FeatureCalcException(e);
			}
		}
	
	private double calcOverlapRatioMin( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap, int regionID, boolean mip ) throws FeatureCalcException {

		if (overlap==0.0) {
			return 0.0;
		}
		
		RelationToValue relation = relationToThreshold.create();
		
		double volume = useMax ? calcMaxVolume( obj1, obj2, regionID, relation, nrgIndex, maskValue ) : calcMinVolume( obj1, obj2, regionID, relation, nrgIndex, maskValue );
		return overlap / volume;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getMaskValue() {
		return maskValue;
	}

	public void setMaskValue(int maskValue) {
		this.maskValue = maskValue;
	}

	public boolean isUseMax() {
		return useMax;
	}

	public void setUseMax(boolean useMax) {
		this.useMax = useMax;
	}

	public double getQuantileLow() {
		return quantileLow;
	}

	public void setQuantileLow(double quantileLow) {
		this.quantileLow = quantileLow;
	}

	public double getQuantileHigh() {
		return quantileHigh;
	}

	public void setQuantileHigh(double quantileHigh) {
		this.quantileHigh = quantileHigh;
	}

}
