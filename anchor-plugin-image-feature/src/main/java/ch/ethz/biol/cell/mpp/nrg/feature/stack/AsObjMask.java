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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Treats a channel as an object-mask, assuming binary values of 0 and 255
 * and calls an object-mask feature
 * 
 * @author FEEHANO
 *
 */
public class AsObjMask extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureObjMaskParams> item;
	
	@BeanField
	/** The channel that that forms the binary mask */
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(CacheableParams<FeatureStackParams> params) throws FeatureCalcException {
		
		return params.calcChangeParams(
			item,
			p -> objMaskFromStack(p),
			"obj"
		);
	}
	
	private FeatureObjMaskParams objMaskFromStack( FeatureStackParams p ) {
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		
		ObjMask om = extractObjMask(p);
		paramsObj.setNrgStack( p.getNrgStack() );
		paramsObj.setObjMask( om );
		return paramsObj;
	}
		
	private ObjMask extractObjMask(FeatureStackParams params) {
		Chnl chnl = params.getNrgStack().getChnl(nrgIndex);
		BinaryChnl binary = new BinaryChnl(chnl, BinaryValues.getDefault());
		
		return new ObjMask( binary.binaryVoxelBox() );
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}
}
