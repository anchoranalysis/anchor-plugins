package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class BoundingBoxUtilities {

	/**
	 * Creates an object-mask for a possibly differently sized bounding box that it currently exists within.
	 * 
	 * @param  object the object-mask
	 * @param  boundingBox a possibly differently sized bounding box
	 * @return the object-mask unchanged if the bounding-box is identical to current, otherwise a new object-mask to match the desired bounding-box
	 */
	public static ObjectMask createObjectForBoundingBox( ObjectMask object, BoundingBox boundingBox ) {
		
		if (object.getBoundingBox().equals(boundingBox)) {
			// Nothing to do, bounding-boxes are equal, early exit
			return object;
		}
		
		VoxelBox<ByteBuffer> vbLarge = VoxelBoxFactory.getByte().create( boundingBox.extent() );
		
		BoundingBox bbLocal = object.getBoundingBox().relPosToBox(boundingBox);

		BinaryValuesByte bvb = BinaryValuesByte.getDefault();
		vbLarge.setPixelsCheckMask(
			new ObjectMask( bbLocal, object.binaryVoxelBox() ),
			bvb.getOnByte()
		);
		
		return new ObjectMask(boundingBox, vbLarge, bvb );
	}
}
