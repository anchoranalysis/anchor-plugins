package org.anchoranalysis.plugin.image.feature.bean.object.single.shared;

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
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.init.FeatureInitParamsShared;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Evaluates the object as a pair-feature together with the binary-mask from the shard objects.
 * 
 * @author Owen Feehan
 *
 */
public class PairedWithBinaryChannel extends FeatureSingleObjectWithShared {

	// START BEAN PROPERTIES
	@BeanField
	private FeaturePairObjects item;
	
	// This cannot be initialized in the normal way, as Feature isn't contained in a Shared-Objects
	// container. So instead it's initialized at a later point.
	@BeanField @SkipInit
	private BinaryChnlProvider binaryChnl;
	// END BEAN PROPERTIES
	
	private BinaryChnl chnl;
	
	@Override
	public void beforeCalcCast(FeatureInitParamsShared params) throws InitException {
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
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		return input.forChild().calc(
			item,
			new CalculatePairInput(chnl),
			new ChildCacheName(PairedWithBinaryChannel.class, chnl.hashCode())
		);
	}

	public FeaturePairObjects getItem() {
		return item;
	}

	public void setItem(FeaturePairObjects item) {
		this.item = item;
	}

	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}

	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}
