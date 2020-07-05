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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segmentation.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;


// Thresholds an image, then creates an object out of each
//   connected set of pixels, and then performs another threshold
//   seperately on each object, setting all pixels not contained
//   within the object to black
// WE SHOULD DELETE THIS
public class SgmnObject extends BinarySegmentation {

	// START BEANS
	/** The Thresholder applied on the whole image */
	@BeanField
	private BinarySegmentation imageSgmn;
	
	/** The Thresholder applied on the local object */
	@BeanField
	private BinarySegmentation objectSgmn;
	
	@BeanField @Positive
	private int minNumPixelsImageSgmn = 100;
	// END BEANS
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(
		VoxelBoxWrapper voxelBoxIn,
		BinarySegmentationParameters params,
		Optional<ObjectMask> mask
	) throws SgmnFailedException {
	
		if (mask.isPresent()) {
			throw new SgmnFailedException("Masks are not supported on this operation");
		}
		
		// Keep a copy of the original unchanged
		VoxelBox<?> orig = voxelBoxIn.any().duplicate();
		
		imageSgmn.sgmn( voxelBoxIn, params, Optional.empty() );

		VoxelBox<ByteBuffer> out = voxelBoxIn.asByte();
		
		sgmnByObj(
			new BinaryVoxelBoxByte(out, BinaryValues.getDefault()),
			new VoxelBoxWrapper(orig),
			params
		);
		
		return new BinaryVoxelBoxByte(out, BinaryValues.getDefault());
	}
	
	private void sgmnByObj(
		BinaryVoxelBox<ByteBuffer> voxelBox,
		VoxelBoxWrapper orig,
		BinarySegmentationParameters params
	) throws SgmnFailedException {

		for( ObjectMask obj : objsFromVoxelBox(voxelBox)) {
			
			if (!obj.numPixelsLessThan(minNumPixelsImageSgmn)) {
				
				BinaryVoxelBox<ByteBuffer> out = objectSgmn.sgmn(
					orig,
					params,
					Optional.of(obj)
				);
				
				if (out==null) {
					continue;
				}
				
				out.copyPixelsToCheckMask(
					new BoundingBox(obj.getBoundingBox().extent()),
					voxelBox.getVoxelBox(),
					obj.getBoundingBox(),
					obj.getVoxelBox(),
					obj.getBinaryValuesByte()
				);
				
			} else {
				voxelBox.setPixelsCheckMaskOff( obj );
			}
		}		
	}
		
	private static ObjectCollection objsFromVoxelBox( BinaryVoxelBox<ByteBuffer> buffer ) throws SgmnFailedException {
		try {
			CreateFromConnectedComponentsFactory omcCreator = new CreateFromConnectedComponentsFactory();
			return omcCreator.createConnectedComponents(buffer.duplicate() );
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	public int getMinNumPixelsImageSgmn() {
		return minNumPixelsImageSgmn;
	}


	public void setMinNumPixelsImageSgmn(int minNumPixelsImageSgmn) {
		this.minNumPixelsImageSgmn = minNumPixelsImageSgmn;
	}
	
	public BinarySegmentation getImageSgmn() {
		return imageSgmn;
	}


	public void setImageSgmn(BinarySegmentation imageSgmn) {
		this.imageSgmn = imageSgmn;
	}


	public BinarySegmentation getObjectSgmn() {
		return objectSgmn;
	}


	public void setObjectSgmn(BinarySegmentation objectSgmn) {
		this.objectSgmn = objectSgmn;
	}
}
