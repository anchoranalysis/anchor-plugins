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
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

// Zeroes voxels outside the mask
public class SgmnZeroOutsideMask extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, RandomNumberGenerator re) throws SgmnFailedException {
		return sgmn.sgmn(voxelBox, params, re);
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, ObjMask objMask, RandomNumberGenerator re) throws SgmnFailedException {
		
		VoxelBox<ByteBuffer> voxelBoxByte = voxelBox.asByte();
		
		VoxelBox<ByteBuffer> destBuffer = VoxelBoxFactory.instance().getByte().create( objMask.getBoundingBox().extnt() ); 
		BoundingBox fullExtnt = new BoundingBox( new Point3i(0,0,0), objMask.getBoundingBox().extnt() );
		
		voxelBoxByte.copyPixelsToCheckMask(
			objMask.getBoundingBox(),
			destBuffer,
			fullExtnt,
			objMask.getVoxelBox(),
			objMask.getBinaryValuesByte()
		);
		
		return sgmn.sgmn( new VoxelBoxWrapper(destBuffer.duplicate()), params, re );
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}
}
