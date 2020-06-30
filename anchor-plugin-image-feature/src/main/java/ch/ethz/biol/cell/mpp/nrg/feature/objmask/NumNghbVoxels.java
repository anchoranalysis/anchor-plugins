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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3NghbMatchValue;

public class NumNghbVoxels extends FeatureSingleObject {

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
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

		FeatureInputSingleObject inputSessionless = input.get();
		
		ObjectMask om = inputSessionless.getObjMask();
		
		VoxelBox<ByteBuffer> vb;
		try {
			vb = inputSessionless
				.getNrgStackRequired()
				.getChnl(nrgIndex).getVoxelBox().asByte();
			
		} catch (IncorrectVoxelDataTypeException e) {
			throw new FeatureCalcException( String.format("nrgStack channel %d has incorrect data type",nrgIndex), e);
		}

		BinaryVoxelBox<ByteBuffer> bvbNotObject = binarize( vb );
				
		OutlineKernel3NghbMatchValue kernelMatch = new OutlineKernel3NghbMatchValue(outsideAtThreshold, do3D, om, bvbNotObject, ignoreAtThreshold);
		return ApplyKernel.applyForCount(kernelMatch, om.getVoxelBox());
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

	public void setDo3D(boolean do3D) {
		this.do3D = do3D;
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
