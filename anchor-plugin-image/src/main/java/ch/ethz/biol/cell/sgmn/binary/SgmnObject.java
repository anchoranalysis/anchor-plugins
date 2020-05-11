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
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;


// Thresholds an image, then creates an object out of each
//   connected set of pixels, and then performs another threshold
//   seperately on each object, setting all pixels not contained
//   within the object to black
// WE SHOULD DELETE THIS
public class SgmnObject extends BinarySgmn {

	// START BEANS
	// The Thresholder applied on the whole image
	@BeanField
	private BinarySgmn imageSgmn;
	
	// The Thresholder applied on the local object
	
	@BeanField
	private BinarySgmn objectSgmn;
	
	@BeanField @Positive
	private int minNumPixelsImageSgmn = 100;
	// END BEANS

	public BinarySgmn getImageSgmn() {
		return imageSgmn;
	}


	public void setImageSgmn(BinarySgmn imageSgmn) {
		this.imageSgmn = imageSgmn;
	}


	public BinarySgmn getObjectSgmn() {
		return objectSgmn;
	}


	public void setObjectSgmn(BinarySgmn objectSgmn) {
		this.objectSgmn = objectSgmn;
	}

	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBoxIn, BinarySgmnParameters params) throws SgmnFailedException {
		
		
		
		VoxelBox<?> orig = voxelBoxIn.any().duplicate();
		
		imageSgmn.sgmn( voxelBoxIn, params );

		VoxelBox<ByteBuffer> voxelBoxInByte = voxelBoxIn.asByte();
		BinaryVoxelBox<ByteBuffer> voxelBox = new BinaryVoxelBoxByte(voxelBoxInByte);
		
		// Main segmentation
		ObjMaskCollection omc;
		try {
			omc = createObjMaskCollectionFromVoxelBox(voxelBox);
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
		
		for( ObjMask objMask : omc) {
			
			if (!objMask.numPixelsLessThan(minNumPixelsImageSgmn)) {
				
				BinaryVoxelBox<ByteBuffer> out = objectSgmn.sgmn(new VoxelBoxWrapper(orig), params, objMask);
				if (out==null) {
					continue;
				}
				
				out.copyPixelsToCheckMask(
					new BoundingBox(objMask.getBoundingBox().extnt()),
					voxelBox.getVoxelBox(),
					objMask.getBoundingBox(),
					objMask.getVoxelBox(),
					objMask.getBinaryValuesByte()
				);
				
			} else {
				voxelBox.setPixelsCheckMaskOff( objMask );
			}
		}
		return new BinaryVoxelBoxByte(voxelBoxInByte);
	}
	
	public static ObjMaskCollection createObjMaskCollectionFromVoxelBox( BinaryVoxelBox<ByteBuffer> buffer ) throws CreateException {
		
		CreateFromConnectedComponentsFactory omcCreator = new CreateFromConnectedComponentsFactory();
		return omcCreator.createConnectedComponents(buffer.duplicate() );
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, ObjMask objMask) {
		throw new IllegalArgumentException("Method not supported");
	}


	public int getMinNumPixelsImageSgmn() {
		return minNumPixelsImageSgmn;
	}


	public void setMinNumPixelsImageSgmn(int minNumPixelsImageSgmn) {
		this.minNumPixelsImageSgmn = minNumPixelsImageSgmn;
	}
}
