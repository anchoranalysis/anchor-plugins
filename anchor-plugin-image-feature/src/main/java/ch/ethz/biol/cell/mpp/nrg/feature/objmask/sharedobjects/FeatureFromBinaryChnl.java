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
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMaskSharedObjects;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.init.FeatureInitParamsSharedObjs;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

/**
 * Creates an object from the binary-mask from a binaryImgChnlProvider and evaluates a feature
 * 
 * @author Owen Feehan
 *
 */
public class FeatureFromBinaryChnl extends FeatureObjMaskSharedObjects {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureObjMaskPair item;
	
	// This cannot be initialized in the normal way, as Feature isn't contained in a Shared-Objects
	// container. So instead it's initialized at a later point.
	@BeanField @SkipInit
	private BinaryChnlProvider binaryChnl;
	// END BEAN PROPERTIES
	
	private BinaryChnl chnl;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsSharedObjs params) throws InitException {
		super.beforeCalcCast(params);
		assert( getLogger()!=null );
		binaryChnl.initRecursive(params.getSharedObjects(), getLogger() );
		
		try {
			chnl = binaryChnl.create();
		} catch (CreateException e) {
			throw new InitException(e);
		}
	}
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {
		return input.forChild().calc(
			item,
			new CalculatePairInput(chnl),
			new ChildCacheName(FeatureFromBinaryChnl.class, chnl.hashCode())
		);
	}

	public FeatureObjMaskPair getItem() {
		return item;
	}

	public void setItem(FeatureObjMaskPair item) {
		this.item = item;
	}

	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}

	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}