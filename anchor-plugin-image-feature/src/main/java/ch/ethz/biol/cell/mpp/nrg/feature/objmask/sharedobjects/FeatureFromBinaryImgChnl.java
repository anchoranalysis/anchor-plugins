package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.cache.FeatureSessionCacheRetriever;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.bean.objmask.sharedobjects.FeatureObjMaskSharedObjects;
import org.anchoranalysis.image.feature.init.FeatureInitParamsImageInit;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;

/**
 * Creates an object from the binary-mask from a binaryImgChnlProvider and evaluates a feature
 * 
 * @author Owen Feehan
 *
 */
public class FeatureFromBinaryImgChnl extends FeatureObjMaskSharedObjects {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureObjMaskPair item;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	// END BEAN PROPERTIES

	private ObjMask objFromBinary = null;

	
	@Override
	public void beforeCalcCast(FeatureInitParamsImageInit params, FeatureSessionCacheRetriever session)
			throws InitException {
		super.beforeCalcCast(params, session);
		assert( getLogger()!=null );
		binaryImgChnlProvider.initRecursive(params.getSharedObjects(), getLogger() );
	}
	
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {

		try {
			// First time it's hit we calculate the bboxRTree
			if (objFromBinary==null) {
				BinaryChnl bic = binaryImgChnlProvider.create();
				objFromBinary = new ObjMask( bic.binaryVoxelBox() );
			}
	
			FeatureObjMaskPairParams paramsPairs = new FeatureObjMaskPairParams();
			paramsPairs.setObjMask1( params.getParams().getObjMask() );
			paramsPairs.setObjMask2( objFromBinary );
			paramsPairs.setNrgStack( params.getParams().getNrgStack() );
			
			return getCacheSession().calc(
				item,
				params.changeParams(paramsPairs)
			);
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	public FeatureObjMaskPair getItem() {
		return item;
	}


	public void setItem(FeatureObjMaskPair item) {
		this.item = item;
	}


	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}


	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}










}
