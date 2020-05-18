package ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.grayscalereconstruction;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

// Self-made grayscale reconstruction by erosion
// See
//   * Vincent paper ("Morphological grayscale reconstruction in image analysis: applications and efficient algorithms")
//   * Powerpoint Presentation by Gonzalez and Woods "Lecture 5: Morphological Image Processing"
public class GrayscaleReconstruction3DVincent extends GrayscaleReconstructionByErosion {

	@Override
	public VoxelBoxWrapper reconstruction( VoxelBoxWrapper mask, VoxelBoxWrapper marker, Optional<ObjMask> containingMask) throws OperationFailedException {
		
		if (containingMask.isPresent()) {
			throw new OperationFailedException("A mask is not supported for this operation");
		}
		
		VoxelBox<ByteBuffer> in = marker.asByte();
		VoxelBox<ByteBuffer> maskCast = mask.asByte();
		
		//int i= 0;
		do {
			//System.out.printf("Iter %d\n",i++);
			VoxelBox<ByteBuffer> vbOut = VoxelBoxFactory.instance().getByte().create( maskCast.extnt() );
			
			// Erode
			if (!GrayscaleErosion.grayscaleErosion( in, vbOut )) {
				//break;
			}
		
			// Take the max between each pixel and the marker Image
			vbOut.max( maskCast );
			
			if (vbOut.equalsDeep(in)) {
				break;
			}
			
			in = vbOut;
			
		} while(true);
		
		return new VoxelBoxWrapper(in);
	}
}
