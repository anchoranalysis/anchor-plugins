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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnOne;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnInv extends BinarySgmnOne {

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmnFromSgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, Optional<ObjMask> mask, BinarySgmn sgmn) throws SgmnFailedException {
		
		BinaryVoxelBox<ByteBuffer> vb = sgmn.sgmn(voxelBox, params, mask);
		
		try {
			invertVoxelBox( vb );
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
		
		return vb;
	}

	private void invertVoxelBox(BinaryVoxelBox<ByteBuffer> voxelBox) throws OperationFailedException {
		
		BinaryValuesByte bv = voxelBox.getBinaryValues().createByte();
		
		int volumeXY = voxelBox.extent().getVolumeXY();
		
		// We invert each item in the VoxelBox
		for( int z=0; z<voxelBox.extent().getZ(); z++) {
			
			ByteBuffer bb = voxelBox.getPixelsForPlane(z).buffer();
			for( int index = 0; index<volumeXY; index++) {
				
				byte val = bb.get(index);
				
				if (val==bv.getOnByte()) {
					bb.put(index, bv.getOffByte());
				} else if (val==bv.getOffByte()) {
					bb.put(index, bv.getOnByte());
				} else {
					assert false;
				}
			}
			
		}		
	}
}
