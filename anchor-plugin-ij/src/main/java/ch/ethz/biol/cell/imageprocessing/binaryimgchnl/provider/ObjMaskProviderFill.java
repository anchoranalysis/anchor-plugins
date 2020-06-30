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
import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Fills holes in an object. Existing obj-masks are overwritten (i.e. their memory buffers are replaced with filled-in pixels).
 * 
 * <p>An optional mask which restricts where a fill operation can happen</p>
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderFill extends ObjectCollectionProviderOne {

	// START BEAN PROPERTIES
	/**  */
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjs( ObjectCollection objsCollection ) throws CreateException {
		
		Optional<BinaryChnl> maskChnl = OptionalFactory.create(mask);
		
		for( ObjectMask om : objsCollection ) {
			BinaryVoxelBox<ByteBuffer> bvb = om.binaryVoxelBox();
			
			
			BinaryVoxelBox<ByteBuffer> bvbDup = bvb.duplicate();
			try {
				BinaryChnlProviderIJBinary.fill(bvbDup);
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
			
			if (maskChnl.isPresent()) {
				// Let's make an object for our mask
				ObjectMask omMask = maskChnl.get().region(om.getBoundingBox(), true);
				
				BoundingBox bboxAll = new BoundingBox( bvb.extent() );
				
				// We do an and operation with the mask
				bvbDup.copyPixelsToCheckMask(bboxAll, bvb.getVoxelBox(), bboxAll, omMask.getVoxelBox(), omMask.getBinaryValuesByte());
			}
			
		}
		
		return objsCollection;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
