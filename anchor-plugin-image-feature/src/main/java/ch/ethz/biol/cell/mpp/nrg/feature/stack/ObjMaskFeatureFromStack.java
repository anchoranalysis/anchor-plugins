package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import cern.colt.list.DoubleArrayList;

public abstract class ObjMaskFeatureFromStack extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureObjMaskParams> item;
	
	@BeanField
	@SkipInit
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureStackParams> paramsCacheable) throws FeatureCalcException {
			
		ObjMaskCollection objsCollection = createObjs(
			paramsCacheable.getParams().getSharedObjs()
		);
		
		DoubleArrayList featureVals = new DoubleArrayList();
		
		// Calculate a feature on each obj mask
		for( int i=0; i<objsCollection.size(); i++) {
			
			ObjMask om = objsCollection.get(i);

			double val = paramsCacheable.calcChangeParams(
				item,
				p -> deriveParams(p, om),
				"objs" + i
			);
			featureVals.add(val);
		}
		
		return deriveStatistic(featureVals);
	}
	
	protected abstract double deriveStatistic( DoubleArrayList featureVals );
	
	private static FeatureObjMaskParams deriveParams( FeatureStackParams params, ObjMask om ) {
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		paramsObj.setNrgStack( params.getNrgStack() );
		paramsObj.setObjMask(om);
		return paramsObj;
	}
		
	private ObjMaskCollection createObjs( ImageInitParams params ) throws FeatureCalcException {

		try {
			objs.initRecursive(params, getLogger() );
			return objs.create();
		} catch (CreateException | InitException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public Feature<FeatureObjMaskParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureObjMaskParams> item) {
		this.item = item;
	}


	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}