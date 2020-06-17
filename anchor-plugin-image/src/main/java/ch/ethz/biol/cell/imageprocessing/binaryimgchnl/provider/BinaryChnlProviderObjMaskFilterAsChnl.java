package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjectFilter;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.factory.CreateFromEntireChnlFactory;

// Treats the entire binaryimgchnl as an object, and sees if it passes an ObjMaskFilter
public class BinaryChnlProviderObjMaskFilterAsChnl extends BinaryChnlProviderElseBase {

	// START BEAN PROPERTIES
	@BeanField
	private ObjectFilter objMaskFilter;
	// END BEAN PROPERTIES

	@Override
	protected boolean condition(BinaryChnl chnl) throws CreateException {

		ObjectMask om = CreateFromEntireChnlFactory.createObjMask( chnl );
		
		ObjectCollection omc = new ObjectCollection(om);
		
		try {
			objMaskFilter.filter(
				omc,
				Optional.of(chnl.getDimensions()),
				Optional.empty()
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		return omc.size()==1;
	}

	public ObjectFilter getObjMaskFilter() {
		return objMaskFilter;
	}

	public void setObjMaskFilter(ObjectFilter objMaskFilter) {
		this.objMaskFilter = objMaskFilter;
	}
}
