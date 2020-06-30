package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import java.util.Optional;

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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objectmask.ObjectCollection;

import cern.colt.list.DoubleArrayList;

public abstract class ObjMaskFeatureFromStack extends FeatureStack {

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleObject> item;
	
	@BeanField
	@SkipInit
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	// We cache the objsCollection as it's not dependent on individual parameters
	private ObjectCollection objsCollection;
	
	@Override
	public double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {
		
		Optional<ImageInitParams> sharedObjs = input.get().getSharedObjs();
		
		if (!sharedObjs.isPresent()) {
			throw new FeatureCalcException("No ImageInitParams are associated with the FeatureStackParams but they are required");
		}
		
		if (objsCollection==null) {
			objsCollection = createObjs(sharedObjs.get());
		}
	
		return deriveStatistic(
			featureValsForObjs(item, input, objsCollection)
		);
	}
	
	protected abstract double deriveStatistic( DoubleArrayList featureVals );
		
	private ObjectCollection createObjs( ImageInitParams params ) throws FeatureCalcException {

		try {
			objs.initRecursive(params, getLogger() );
			return objs.create();
		} catch (CreateException | InitException e) {
			throw new FeatureCalcException(e);
		}
	}

	private DoubleArrayList featureValsForObjs(
		Feature<FeatureInputSingleObject> feature,
		SessionInput<FeatureInputStack> input,
		ObjectCollection objsCollection
	) throws FeatureCalcException {
		DoubleArrayList featureVals = new DoubleArrayList();
		
		// Calculate a feature on each obj mask
		for( int i=0; i<objsCollection.size(); i++) {

			double val = input.forChild().calc(
				feature,
				new CalculateInputFromStack(objsCollection, i),
				cacheName(i)
			);
			featureVals.add(val);
		}
		return featureVals;
	}
	
	private ChildCacheName cacheName(int index) {
		return new ChildCacheName(
			ObjMaskFeatureFromStack.class,
			index + "_" + objsCollection.hashCode()
		);
	}
	
	public Feature<FeatureInputSingleObject> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleObject> item) {
		this.item = item;
	}


	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}
