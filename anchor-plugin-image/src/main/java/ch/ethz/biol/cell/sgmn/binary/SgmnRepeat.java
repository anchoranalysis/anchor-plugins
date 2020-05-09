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
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnRepeat extends BinarySgmn {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField @Positive
	private int maxIter = 10;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params) {
		throw new IllegalArgumentException("Invalid method");
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params, ObjMask objMask) throws SgmnFailedException {
		
		BinaryVoxelBox<ByteBuffer> outOld = null;
		BoundingBox BoundingBox = new BoundingBox( objMask.getBoundingBox() );
		
		int cnt = 0;
		while (cnt++<maxIter) {
			BinaryVoxelBox<ByteBuffer> outNew = sgmn.sgmn(voxelBox, params, objMask);
			
			if (outNew==null) {
				return outOld;
			}
			
			outOld = outNew;
			objMask = new ObjMask(BoundingBox, outNew);
		}
		
		return outOld;
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}
}
