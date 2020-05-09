package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import java.util.function.BiFunction;

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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobal;

public abstract class OverlapMaskBase extends FeaturePairMemo {

	// START BEAN PROPERTIES
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
		
	protected double overlapForRegion( SessionInput<FeatureInputPairMemo> input, int regionID ) throws FeatureCalcException {
		return input.calc(
			new OverlapCalculationMaskGlobal(regionID, nrgIndex, (byte) maskValue)
		);
	}

	protected double calcMinVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationBean relationToThreshold
	) throws FeatureCalcException {
		return calcVolumeStat(
			obj1,
			obj2,
			regionID,
			relationToThreshold,
			Math::min
		);
	}

	protected double calcMaxVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationBean relationToThreshold
	) throws FeatureCalcException {
		
		return calcVolumeStat(
			obj1,
			obj2,
			regionID,
			relationToThreshold,
			Math::max
		);
	}
	
	protected double calcVolumeStat(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationBean relationToThreshold,
		BiFunction<Long,Long,Long> statFunc
	) throws FeatureCalcException {
		
		long size1 = sizeForObj(obj1, regionID, relationToThreshold);
		long size2 = sizeForObj(obj2, regionID, relationToThreshold);
		return statFunc.apply(size1, size2);
	}
	
	private long sizeForObj( PxlMarkMemo obj, int regionID, RelationBean relationToThreshold) {
		VoxelStatistics pxlStats =  obj.doOperation().statisticsForAllSlices(nrgIndex, regionID);
		return pxlStats.countThreshold(
			new RelationToConstant(
				relationToThreshold,
				maskValue
			)
		);
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
