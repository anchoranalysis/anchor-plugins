package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.BinaryChnlFromObjs;

/** 
 * Creates a BinaryImgChannel from a collection of object masks
 **/
public class BinaryChnlProviderFromObjMasks extends BinaryChnlProviderDimSource {

	// START BEAN
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private boolean invert = false;
	// END BEAN

	@Override
	protected BinaryChnl createFromSource(ImageDim dimSource) throws CreateException {

		ObjMaskCollection objCollection = objs.create();
		if (objCollection==null) {
			throw new CreateException("objMaskProvider returned null");
		}
		return create( objCollection, dimSource, invert );
	}
	
	private static BinaryChnl create(  ObjMaskCollection objs, ImageDim dim, boolean invert ) throws CreateException {

		BinaryValues bv = BinaryValues.getDefault();
		
		BinaryChnl maskedImage = createChnlFromObjsMux(objs, dim, bv, invert);
		return new BinaryChnl(maskedImage.getChnl(), bv);	
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
	
	private static BinaryChnl createChnlFromObjsMux( ObjMaskCollection objs, ImageDim sd, BinaryValues outVal, boolean invert ) throws CreateException {
		if (invert) {
			return BinaryChnlFromObjs.createFromNotObjs(objs, sd, outVal);
		} else {
			return BinaryChnlFromObjs.createFromObjs(objs, sd, outVal);
		}
	}
}
