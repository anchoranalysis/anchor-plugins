package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

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

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNghbIgnoreOutsideScene;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateOutlineNumVoxelFaces extends CachedCalculationCastParams<Integer,FeatureObjMaskParams> {

	/**
	 * Whether to calculate the outline on a MIP
	 */
	private boolean mip=false;
	
	/**
	 * Whether to suppress 3D calculations (only consider XY neighbours). Doesn't make sense if mip=TRUE, and will then be ignroed.
	 */
	private boolean suppress3D=false;
			
	public CalculateOutlineNumVoxelFaces(boolean mip, boolean suppress3D) {
		super();
		this.mip = mip;
		this.suppress3D = suppress3D;
	}
	
	private static int calcSurfaceSize( ObjMask objMask, ImageDim dim, boolean mip, boolean suppress3D ) throws OperationFailedException {
		
		boolean do3D = (dim.getZ() > 1) && !suppress3D;
		
		if (do3D && mip) {
			// If we're in 3D mode AND MIP mode, then we get a maximum intensity projection
			CountKernel kernel = new CountKernelNghbIgnoreOutsideScene(false, objMask.getBinaryValuesByte(), true, dim.getExtnt(), objMask.getBoundingBox().getCrnrMin() );
			
			VoxelBox<ByteBuffer> mipVb = objMask.getVoxelBox().maxIntensityProj();
			return ApplyKernel.applyForCount(kernel, mipVb );
			
		} else {
			CountKernel kernel = new CountKernelNghbIgnoreOutsideScene(do3D, objMask.getBinaryValuesByte(), true, dim.getExtnt(), objMask.getBoundingBox().getCrnrMin() );
			return ApplyKernel.applyForCount(kernel, objMask.getVoxelBox() );
		}
	}
	

	@Override
	protected Integer execute(FeatureObjMaskParams params)
			throws ExecuteException {
		try {
			return calcSurfaceSize(params.getObjMask(), params.getNrgStack().getDimensions(), mip, suppress3D);
		} catch (OperationFailedException e) {
			throw new ExecuteException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CalculateOutlineNumVoxelFaces){
	        final CalculateOutlineNumVoxelFaces other = (CalculateOutlineNumVoxelFaces) obj;
	        return new EqualsBuilder()
	            .append(mip, other.mip)
	            .append(suppress3D, other.suppress3D)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public CachedCalculation<Integer> duplicate() {
		return new CalculateOutlineNumVoxelFaces(mip,suppress3D);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(mip).append(suppress3D).toHashCode();
	}
	
}