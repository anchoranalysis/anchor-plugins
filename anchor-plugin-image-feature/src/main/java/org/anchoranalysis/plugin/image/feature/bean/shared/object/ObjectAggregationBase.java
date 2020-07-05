package org.anchoranalysis.plugin.image.feature.bean.shared.object;



/*-
 * #%L
 * anchor-plugin-image-feature
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.init.FeatureInitParamsShared;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

import cern.colt.list.DoubleArrayList;

/**
 * Calculates a feature for a set of objects using this stack as a NRG-stack, and aggregates.
 * 
 * @author Owen Feehan
 * @param <T> feature-input
 */
public abstract class ObjectAggregationBase<T extends FeatureInputNRG> extends FeatureSingleObjectFromShared<T> {

	// START BEAN PROPERTIES
	@BeanField
	@SkipInit
	private ObjectCollectionProvider objs;
	// END BEAN PROPERTIES
	
	// We cache the objsCollection as it's not dependent on individual parameters
	private ObjectCollection objsCollection;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsShared params) throws InitException {
		objs.initRecursive(params.getSharedObjects(), getLogger() );
	}
	
	@Override
	protected double calc(CalcForChild<T> calcForChild, Feature<FeatureInputSingleObject> featureForSingleObject) throws FeatureCalcException {

		if (objsCollection==null) {
			objsCollection = createObjs();
		}
	
		return deriveStatistic(
			featureValsForObjs(featureForSingleObject, calcForChild, objsCollection)
		);
	}
	
	protected abstract double deriveStatistic( DoubleArrayList featureVals );
		
	private ObjectCollection createObjs() throws FeatureCalcException {
		try {
			return objs.create();
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	private DoubleArrayList featureValsForObjs(
		Feature<FeatureInputSingleObject> feature,
		CalcForChild<T> calcForChild,
		ObjectCollection objsCollection
	) throws FeatureCalcException {
		DoubleArrayList featureVals = new DoubleArrayList();
		
		// Calculate a feature on each obj mask
		for( int i=0; i<objsCollection.size(); i++) {

			double val = calcForChild.calc(
				feature,
				new CalculateInputFromStack<>(objsCollection, i),
				cacheName(i)
			);
			featureVals.add(val);
		}
		return featureVals;
	}
	
	private ChildCacheName cacheName(int index) {
		return new ChildCacheName(
			ObjectAggregationBase.class,
			index + "_" + objsCollection.hashCode()
		);
	}

	public ObjectCollectionProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjectCollectionProvider objs) {
		this.objs = objs;
	}
}
