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
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnOne;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnRepeat extends BinarySgmnOne {

	// START BEAN PROPERTIES
	@BeanField @Positive
	private int maxIter = 10;
	// END BEAN PROPERTIES

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmnFromSgmn(
		VoxelBoxWrapper voxelBox,
		BinarySgmnParameters params,
		Optional<ObjectMask> mask,
		BinarySgmn sgmn
	) throws SgmnFailedException {
		
		BinaryVoxelBox<ByteBuffer> outOld = null;
		
		int cnt = 0;
		while (cnt++<maxIter) {
			BinaryVoxelBox<ByteBuffer> outNew = sgmn.sgmn(voxelBox, params, mask);
			
			if (outNew==null) {
				return outOld;
			}
			
			outOld = outNew;
			
			// Increasingly tightens the obj-mask used for the segmentation
			mask = Optional.of(
				mask.isPresent() ? new ObjectMask(mask.get().getBoundingBox(), outNew) : new ObjectMask(outNew)
			);
		}
		
		return outOld;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}
}
