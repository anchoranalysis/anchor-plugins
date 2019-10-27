package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3NghbMatchValue;

public class NumNghbVoxels extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean outsideAtThreshold = false;
	
	@BeanField
	private boolean ignoreAtThreshold = false;
	
	@BeanField
	private boolean do3D = false;
	
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
	
	private static BinaryVoxelBox<ByteBuffer> binarize( VoxelBox<ByteBuffer> vb ) throws FeatureCalcException {
		return new BinaryVoxelBoxByte( vb, BinaryValues.getDefault() );
	}
	
	@Override
	public double calcCast(FeatureObjMaskParams params) throws FeatureCalcException {

		ObjMask om = params.getObjMask();

		//OutlineKernel3 kernel = new OutlineKernel3(om.getBinaryValuesByte(), outsideAtThreshold, do3D);
		//int numBorderPixelsA = ApplyBinaryKernel.applyForCount(kernel, om.getVoxelBox());
		
		VoxelBox<ByteBuffer> vb;
		try {
			vb = params.getNrgStack().getNrgStack().getChnl(nrgIndex).getVoxelBox().asByte();
		} catch (IncorrectVoxelDataTypeException e) {
			throw new FeatureCalcException( String.format("nrgStack channel %d has incorrect data type",nrgIndex), e);
		}

		BinaryVoxelBox<ByteBuffer> bvbNotObject = binarize( vb );
				
		OutlineKernel3NghbMatchValue kernelMatch = new OutlineKernel3NghbMatchValue(outsideAtThreshold, do3D, om, bvbNotObject);
		kernelMatch.setIgnoreAtThreshold(ignoreAtThreshold);
		int cnt = ApplyKernel.applyForCount(kernelMatch, om.getVoxelBox());
		
		//double ratio = ((double) cnt) / numBorderPixelsA;
		//System.out.printf("ObjMask at centre %s\n", om.centerOfGravity().toString() );
		//System.out.printf("NumNghbVoxel=%d  NumBorderPixels=%d   ratio=%f\n", cnt, numBorderPixelsA, ratio);
		
		return cnt;
	}

	public boolean isOutsideAtThreshold() {
		return outsideAtThreshold;
	}

	public void setOutsideAtThreshold(boolean outsideAtThreshold) {
		this.outsideAtThreshold = outsideAtThreshold;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public boolean isIgnoreAtThreshold() {
		return ignoreAtThreshold;
	}

	public void setIgnoreAtThreshold(boolean ignoreAtThreshold) {
		this.ignoreAtThreshold = ignoreAtThreshold;
	}




}
