package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

/**
 * Fills holes in an object. Existing obj-masks are overwritten (i.e. their memory buffers are replaced with filled-in pixels).
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderFill extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	/** A mask which restricts where a fill operation can happen */
	@BeanField
	private BinaryImgChnlProvider maskProvider;
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection createFromObjs( ObjMaskCollection objsCollection ) throws CreateException {
		
		BinaryChnl mask = createMaskOrNull();
		
		for( ObjMask om : objsCollection ) {
			BinaryVoxelBox<ByteBuffer> bvb = om.binaryVoxelBox();
			
			
			BinaryVoxelBox<ByteBuffer> bvbDup = bvb.duplicate();
			try {
				BinaryImgChnlProviderIJBinary.fill(bvbDup);
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
			
			if (mask!=null) {
				// Let's make an object for our mask
				ObjMask omMask = mask.createMaskAvoidNew(om.getBoundingBox());
				
				BoundingBox bboxAll = new BoundingBox( bvb.extnt() );
				
				// We do an and operation with the mask
				bvbDup.copyPixelsToCheckMask(bboxAll, bvb.getVoxelBox(), bboxAll, omMask.getVoxelBox(), omMask.getBinaryValuesByte());
			}
			
		}
		
		return objsCollection;
	}
	
	private BinaryChnl createMaskOrNull() throws CreateException {
		if (maskProvider!=null) {
			return maskProvider.create();
		} else {
			return null;
		}
	}

	public BinaryImgChnlProvider getMaskProvider() {
		return maskProvider;
	}

	public void setMaskProvider(BinaryImgChnlProvider maskProvider) {
		this.maskProvider = maskProvider;
	}

}
