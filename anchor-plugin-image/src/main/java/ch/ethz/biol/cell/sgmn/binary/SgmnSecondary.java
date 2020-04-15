package ch.ethz.biol.cell.sgmn.binary;

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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactoryTypeBound;

// Performs a primary segmentation on the incoming voxels
// And a secondary segmentation on all the voxels which were rejected by
//   the primary segmentation
public class SgmnSecondary extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField @OptionalBean
	private BinarySgmn sgmnSecondary;	// Applied on pixels rejected from the first segmentation
	
	@BeanField
	private boolean onFailureKeepOld = false;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, RandomNumberGenerator re) {
		throw new IllegalArgumentException("Method not supported yet");
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params, ObjMask objMask, RandomNumberGenerator re) throws SgmnFailedException {

		VoxelBox<ByteBuffer> voxelBoxByte = voxelBox.asByte();
		
		BinaryVoxelBox<ByteBuffer> out = sgmn.sgmn( voxelBox, params, objMask, re );
		
		if (out==null) {
			return null;
		}
		
		if (sgmnSecondary!=null) {
			
			System.out.println("Secondary Segmentation");
			
			Extent e = objMask.getBoundingBox().extnt();
			BoundingBox bboxE = new BoundingBox(e);
			
			VoxelBoxFactoryTypeBound<ByteBuffer> factory = VoxelBoxFactory.getByte();
			
			// We create a new image which is the orig (obj Masked) minus the areas which have survived the second segmentation
			VoxelBox<ByteBuffer> diffBuffer = factory.create( e );
			VoxelBox<ByteBuffer> diffBufferMask = factory.create( e );
			voxelBoxByte.copyPixelsToCheckMask( objMask.getBoundingBox(), diffBuffer, bboxE, objMask.getVoxelBox(), objMask.getBinaryValuesByte() );
			objMask.getVoxelBox().copyPixelsToCheckMask( bboxE, diffBufferMask, bboxE, objMask.getVoxelBox(), objMask.getBinaryValuesByte() );
			
			//VoxelBox<ByteBuffer> secondSgmnBuffer = factoryVoxelBox.create( e );
			//o.copyPixelsToCheckMask( objMask.getBoundingBox(), secondSgmnBuffer, bboxE, objMask.getVoxelBox() );
			
			ObjMask om = new ObjMask(bboxE, out);
			diffBuffer.setPixelsCheckMask( om, 0);
			diffBufferMask.setPixelsCheckMask( om, 0);
			
			// TODO SPEED THIS UP, GET SUCC WORKING BETTER, use better function than countGreaterThant
			// more than
			if (diffBuffer.hasGreaterThan(0)) {
			
				ObjMask diffOM = new ObjMask(bboxE,diffBufferMask);
				BinaryVoxelBox<ByteBuffer> outSecondary = sgmnSecondary.sgmn( new VoxelBoxWrapper(diffBuffer), params, diffOM, re );
				if (outSecondary!=null) {
					System.out.println("Rescuing lost item");
					outSecondary.copyPixelsToCheckMask(bboxE, out.getVoxelBox(), bboxE, diffBufferMask, diffOM.getBinaryValuesByte() );
				}
				return out;
			}
			
			// Ok let's overwrite the second segmentation for now
		}
		return out;
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}

	public BinarySgmn getSgmnSecondary() {
		return sgmnSecondary;
	}

	public void setSgmnSecondary(BinarySgmn sgmnSecondary) {
		this.sgmnSecondary = sgmnSecondary;
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return sgmn.getAdditionalOutput();
	}
}
