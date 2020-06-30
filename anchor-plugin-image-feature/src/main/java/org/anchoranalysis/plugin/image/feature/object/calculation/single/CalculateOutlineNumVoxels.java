package org.anchoranalysis.plugin.image.feature.object.calculation.single;

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

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateOutlineNumVoxels extends FeatureCalculation<Integer,FeatureInputSingleObject> {

	/**
	 * Whether to calculate the outline on a MIP
	 */
	private boolean mip=false;
	
	/**
	 * Whether to suppress 3D calculations (only consider XY neighbours). Doesn't make sense if mip=TRUE, and will then be ignroed.
	 */
	private boolean suppress3D=false;
			
	public CalculateOutlineNumVoxels(boolean mip, boolean suppress3D) {
		super();
		this.mip = mip;
		this.suppress3D = suppress3D;
	}
	
	private static int calcSurfaceSize( ObjectMask objMask, ImageDim dim, boolean mip, boolean suppress3D ) {
		
		boolean do3D = (dim.getZ() > 1) && !suppress3D;
		
		if (do3D && mip) {
			// If we're in 3D mode AND MIP mode, then we get a maximum intensity projection
			
			OutlineKernel3 kernel = new OutlineKernel3(objMask.getBinaryValuesByte(), false, false);
			
			VoxelBox<ByteBuffer> mipVb = objMask.getVoxelBox().maxIntensityProj();
			return ApplyKernel.applyForCount(kernel, mipVb );
			
		} else {
			OutlineKernel3 kernel = new OutlineKernel3(objMask.getBinaryValuesByte(), false, do3D);
			return ApplyKernel.applyForCount(kernel, objMask.getVoxelBox() );
		}
	}
	

	@Override
	protected Integer execute(FeatureInputSingleObject input)	throws FeatureCalcException {
		return calcSurfaceSize(input.getObjMask(), input.getDimensionsRequired(), mip, suppress3D);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CalculateOutlineNumVoxels){
	        final CalculateOutlineNumVoxels other = (CalculateOutlineNumVoxels) obj;
	        return new EqualsBuilder()
	            .append(mip, other.mip)
	            .append(suppress3D, other.suppress3D)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(mip).append(suppress3D).toHashCode();
	}
	
}