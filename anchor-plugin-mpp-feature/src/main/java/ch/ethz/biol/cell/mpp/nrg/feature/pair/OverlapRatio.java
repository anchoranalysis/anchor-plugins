package ch.ethz.biol.cell.mpp.nrg.feature.pair;

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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.NRGElemPairCalcParams;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculation;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapMIPRatioCalculation;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPair;

public class OverlapRatio extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private boolean mip = false;
	
	@BeanField
	private boolean useMax = false;
	// END BEAN PROPERTIES
	
	public OverlapRatio() {
	}
	
	private CachedCalculation<Double> cc;
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		if (mip) {
			// If we use this we don't need to find the volume ourselves
			cc = cache.search( new OverlapMIPRatioCalculation(regionID) );
		} else {
			cc = cache.search( new OverlapCalculation(regionID) );
		}		
	}
	
	public static double calcMinVolume( PxlMarkMemo obj1, PxlMarkMemo obj2, int regionID ) throws FeatureCalcException {
		try {
			VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(0, regionID);
			VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(0, regionID);
			
			long size1 = pxlStats1.size();
			long size2 = pxlStats2.size();
			return Math.min( size1, size2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	
	public static double calcMaxVolume( PxlMarkMemo obj1, PxlMarkMemo obj2, int regionID ) throws FeatureCalcException {
		try {
			VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(0, regionID);
			VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(0, regionID);
			
			long size1 = pxlStats1.size();
			long size2 = pxlStats2.size();
			return Math.max( size1, size2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public static double calcOverlapRatioMin( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap, int regionID, boolean mip ) throws FeatureCalcException {

		if (overlap==0.0) {
			return 0.0;
		}
		
		if (mip) {
			return overlap;
		} else {
			double volume = calcMinVolume( obj1, obj2, regionID );
			return overlap / volume;
		}
	}
	
	public static double calcOverlapRatioMax( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap, int regionID, boolean mip ) throws FeatureCalcException {
		
		if (overlap==0.0) {
			return 0.0;
		}
		
		if (mip) {
			return overlap;
		} else {
			double volume = calcMaxVolume( obj1, obj2, regionID );
			return overlap / volume;
		}
	}
	
	@Override
	public double calcCast( NRGElemPairCalcParams params ) throws FeatureCalcException {
		
		assert( cc!=null );
		
		try {
			if (useMax) {
				return calcOverlapRatioMax( params.getObj1(), params.getObj2(), cc.getOrCalculate(params), regionID, mip );
			} else {
				return calcOverlapRatioMin( params.getObj1(), params.getObj2(), cc.getOrCalculate(params), regionID, mip );
			}
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}
	
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}

	public boolean isUseMax() {
		return useMax;
	}

	public void setUseMax(boolean useMax) {
		this.useMax = useMax;
	}


}
