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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

// Calculates the median of a feature applied to each connected component
public class MedianConnectedComponentFeature extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature item;
	
	@BeanField
	private int nrgChnlIndex = 0;
	// END BEAN PROPERTIES

	@Override
	public double calcCast(CacheableParams<FeatureStackParams> params) throws FeatureCalcException {

		BinaryChnl binaryImgChnl = new BinaryChnl(
			params.getParams().getNrgStack().getChnl(nrgChnlIndex),
			BinaryValues.getDefault()
		);
		
		ObjMaskCollection omc;
		try {
			CreateFromConnectedComponentsFactory objMaskCreator = new CreateFromConnectedComponentsFactory();
			objMaskCreator.setMinNumberVoxels(1);
			omc = objMaskCreator.createConnectedComponents(binaryImgChnl );
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
		
		
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		paramsObj.setNrgStack( params.getParams().getNrgStack() );
		
		DoubleArrayList featureVals = new DoubleArrayList();
		
		// Calculate a feature on each obj mask
		for( ObjMask om : omc ) {
			paramsObj.setObjMask(om);
			double val = getCacheSession().calc(
				item,
				params.changeParams(paramsObj)
			);
			featureVals.add(val);
		}
		
		featureVals.sort();
		
		return Descriptive.median(featureVals);
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	public int getNrgChnlIndex() {
		return nrgChnlIndex;
	}

	public void setNrgChnlIndex(int nrgChnlIndex) {
		this.nrgChnlIndex = nrgChnlIndex;
	}


}
