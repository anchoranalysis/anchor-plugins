package ch.ethz.biol.cell.mpp.nrg.feature.pair;

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

import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPairCalcParams;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculation;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapMIPRatioCalculation;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPair;

public class OverlapRatioThreeRegions extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID1 = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int regionID2 = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int regionID3 = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private boolean mip = false;
	// END BEAN PROPERTIES
	
	private CachedCalculation<Double> cc1;
	private CachedCalculation<Double> cc2;
	private CachedCalculation<Double> cc3;	
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		if (mip) {
			// If we use this we don't need to find the volume ourselves
			cc1 = cache.search( new OverlapMIPRatioCalculation(regionID1) );
			cc2 = cache.search( new OverlapMIPRatioCalculation(regionID2) );
			cc3 = cache.search( new OverlapMIPRatioCalculation(regionID3) );
		} else {
			cc1 = cache.search( new OverlapCalculation(regionID1) );
			cc2 = cache.search( new OverlapCalculation(regionID2) );
			cc3 = cache.search( new OverlapCalculation(regionID3) );
		}
	}
	
	public OverlapRatioThreeRegions() {
	}
	
	private static double calcMinVolume( PxlMarkMemo obj1, PxlMarkMemo obj2, int regionID ) throws FeatureCalcException {
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
	

	public static double calcOverlapRatioMin( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap1, double overlap2, double overlap3, int regionID1, int regionID2, int regionID3 ) throws FeatureCalcException {

		double overlap = overlap1 + overlap2 + overlap3;
		
		if (overlap==0.0) {
			return 0.0;
		}
		
		double volume1 = calcMinVolume( obj1, obj2, regionID1 );
		double volume2 = calcMinVolume( obj1, obj2, regionID2 );
		double volume3 = calcMinVolume( obj1, obj2, regionID3 );
		return overlap / (volume1+volume2+volume3);
	}
	
	@Override
	public double calcCast( NRGElemPairCalcParams params ) throws FeatureCalcException {
		
		// MIP currently not supported
		if(mip==true) {
			throw new FeatureCalcException("mip currently not supported");
		}
		
		
		assert( cc1!=null );
		assert( cc2!=null );
		assert( cc3!=null );
		
		try {
			return calcOverlapRatioMin( params.getObj1(), params.getObj2(), cc1.getOrCalculate(params), cc2.getOrCalculate(params), cc3.getOrCalculate(params), regionID1, regionID2, regionID3 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}

	public int getRegionID1() {
		return regionID1;
	}

	public void setRegionID1(int regionID1) {
		this.regionID1 = regionID1;
	}

	public int getRegionID2() {
		return regionID2;
	}

	public void setRegionID2(int regionID2) {
		this.regionID2 = regionID2;
	}

	public int getRegionID3() {
		return regionID3;
	}

	public void setRegionID3(int regionID3) {
		this.regionID3 = regionID3;
	}


}
