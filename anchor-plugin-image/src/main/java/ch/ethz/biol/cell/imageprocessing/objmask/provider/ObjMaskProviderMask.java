package ch.ethz.biol.cell.imageprocessing.objmask.provider;



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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.logical.BinaryChnlAnd;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

/**
 * Applies a binary-mask to the object-collection possibly masking each individual object
 * 
 * Note the masking overwrites the current objects in situ (so apply a duplicate operation beforehand
 *   to preserve the existing objects) 
 * 
 * @author feehano
 *
 */
public class ObjMaskProviderMask extends ObjMaskProviderOne {
	
	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider maskProvider;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objsIn) throws CreateException {
	
		BinaryChnl mask = maskProvider.create();
		
		for( ObjMask om : objsIn ) {

			applyMask(om, mask);
		}
		return objsIn;
	}
	
	private static void applyMask( ObjMask om, BinaryChnl mask ) throws CreateException {
		
		// Just the portion of the mask that matches the obunding box of our object
		ObjMask maskObj = mask.createMaskAvoidNew(om.getBoundingBox());
		
		BinaryChnlAnd.apply(
			om.binaryVoxelBox().getVoxelBox(),
			maskObj.binaryVoxelBox().getVoxelBox(),
			om.binaryVoxelBox().getBinaryValues().createByte(),
			maskObj.binaryVoxelBox().getBinaryValues().createByte()
		);		
	}

	public BinaryImgChnlProvider getMaskProvider() {
		return maskProvider;
	}

	public void setMaskProvider(BinaryImgChnlProvider maskProvider) {
		this.maskProvider = maskProvider;
	}

}
