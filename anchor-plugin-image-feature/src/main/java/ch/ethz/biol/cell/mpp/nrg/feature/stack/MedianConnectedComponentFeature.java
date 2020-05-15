package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/** Calculates the median of a feature applied to each connected component */
public class MedianConnectedComponentFeature extends FeatureStack {

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleObj> item;
	
	@BeanField
	private int nrgChnlIndex = 0;
	// END BEAN PROPERTIES

	@Override
	public double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {

		ResolvedCalculation<ObjMaskCollection, FeatureInputStack> ccObjs = input.resolver().search(
			new CalculateConnectedComponents(nrgChnlIndex)
		);
		
		int size = input.calc(ccObjs).size();
						
		DoubleArrayList featureVals = new DoubleArrayList();
		
		// Calculate a feature on each obj mask
		for( int i=0; i<size; i++ ) {
						
			double val = input.forChild().calc(
				item,
				new CalculateDeriveObjFromCollection(ccObjs, i),
				cacheName(i)
			);
			featureVals.add(val);
		}
		
		featureVals.sort();
		
		return Descriptive.median(featureVals);
	}
	
	private ChildCacheName cacheName( int index ) {
		return new ChildCacheName(
			MedianConnectedComponentFeature.class,
			nrgChnlIndex + "_" + index
		);
	}

	public Feature<FeatureInputSingleObj> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleObj> item) {
		this.item = item;
	}

	public int getNrgChnlIndex() {
		return nrgChnlIndex;
	}

	public void setNrgChnlIndex(int nrgChnlIndex) {
		this.nrgChnlIndex = nrgChnlIndex;
	}
}
