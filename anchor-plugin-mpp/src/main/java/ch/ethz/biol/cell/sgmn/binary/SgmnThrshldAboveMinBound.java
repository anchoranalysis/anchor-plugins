package ch.ethz.biol.cell.sgmn.binary;

/*
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.anchor.mpp.mark.bounds.MarkBounds;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnThrshld;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.calculatelevel.Constant;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

// Performs a thresholding that accepts only channel values with intensities
//   greater than the minimum bound
public class SgmnThrshldAboveMinBound extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean suppress3D = false;
	
	@BeanField
	private MarkBounds markBounds;
	// END BEAN PROPERTIES
	
	private BinarySgmnThrshld delegate = new BinarySgmnThrshld();
	
	private void setUpDelegate( Extent e, ImageRes res ) {
		double minBound = markBounds.getMinRslvd( res, e.getZ()>1 && !suppress3D );

		int threshold = (int) Math.floor(minBound);
		
		CalculateLevel calculateLevel = new Constant( threshold );
		
		ThresholderGlobal thresholder = new ThresholderGlobal();
		thresholder.setCalculateLevel(calculateLevel);
		
		delegate.setThresholder( thresholder );
	}
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, RandomNumberGenerator re)
			throws SgmnFailedException {
		setUpDelegate( voxelBox.any().extnt(), params.getRes() );
		return delegate.sgmn(voxelBox, params, re);
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params, ObjMask objMask, RandomNumberGenerator re) throws SgmnFailedException {
		return delegate.sgmn(voxelBox, params, objMask, re);
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return delegate.getAdditionalOutput();
	}

	public boolean isSuppress3D() {
		return suppress3D;
	}

	public void setSuppress3D(boolean suppress3d) {
		suppress3D = suppress3d;
	}

	public MarkBounds getMarkBounds() {
		return markBounds;
	}


	public void setMarkBounds(MarkBounds markBounds) {
		this.markBounds = markBounds;
	}

}
