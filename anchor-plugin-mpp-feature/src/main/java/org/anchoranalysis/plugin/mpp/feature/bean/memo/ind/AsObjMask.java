package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemoDescriptor;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;

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
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.descriptor.FeatureInputDescriptor;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

public class AsObjMask extends FeatureSingleElem<FeatureInputSingleMemo,FeatureInputSingleObj> {

	// START BEAN PROPERTIES
	@BeanField
	private RegionMap regionMap = RegionMapSingleton.instance();
	
	@BeanField
	private int index = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {
		return input
			.forChild()
			.calc(
				getItem(),
				new CalculateSingleObjFromMemo(regionMap,index),
				new ChildCacheName(AsObjMask.class, index)
			);			
	}

	// We change the default behaviour, as we don't want to give the same paramsFactory
	//   as the item we pass to
	@Override
	public FeatureInputDescriptor paramType()
			throws FeatureCalcException {
		return FeatureInputSingleMemoDescriptor.instance;
	}

	@Override
	public String getParamDscr() {
		return getItem().getParamDscr();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public RegionMap getRegionMap() {
		return regionMap;
	}

	public void setRegionMap(RegionMap regionMap) {
		this.regionMap = regionMap;
	}


}
