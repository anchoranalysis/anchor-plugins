package ch.ethz.biol.cell.mpp.nrg.feature.pair;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobal;

public abstract class OverlapMaskBase extends FeaturePairMemo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
		
	protected double overlapForRegion( SessionInput<FeatureInputPairMemo> paramsCacheable, int regionID ) throws FeatureCalcException {
		return paramsCacheable.calc(
			new OverlapCalculationMaskGlobal(regionID, nrgIndex, (byte) maskValue)
		);
	}
	
	protected double calcMinVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationToValue relationToThreshold
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
	
	protected double calcMaxVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationToValue relationToThreshold
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
}
