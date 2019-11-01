package ch.ethz.biol.cell.mpp.nrg.feature.ind;

/*
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.NRGElemInd;
import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParams;

public class SurfaceSizeMaskNonZero extends NRGElemInd {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int maskIndex = 0;
	
	@BeanField
	private RegionMap regionMap = RegionMapSingleton.instance();
	
	@BeanField
	private int regionID = 0;
	
	@BeanField
	private boolean suppressZ = false;;
	// END BEAN PROPERTIES
	
	public static VoxelBox<ByteBuffer> calcSurfaceSize( ObjMask objMask, boolean useZ ) {

		OutlineKernel3 kernel = new OutlineKernel3( objMask.getBinaryValuesByte(), false, useZ );
	
		return ApplyKernel.apply(kernel, objMask.getVoxelBox(), objMask.getBinaryValuesByte() );

		//return buf.countEqual( objMask.getBinaryValues().getOnInt() );
	}

	@Override
	public double calcCast(NRGElemIndCalcParams params)
			throws FeatureCalcException {

//		ObjMask om;
//		try {
//			om = params.getPxlPartMemo().doOperation().getObjMask();
//		} catch (ExecuteException e) {
//			throw new FeatureCalcException(e);
//		}
//		params.getPxlPartMemo().
		
		ObjMaskWithProperties omWithProps = params.getPxlPartMemo().getMark().calcMask(
			params.getNrgStack().getDimensions(),
			regionMap.membershipWithFlagsForIndex(regionID),
			BinaryValuesByte.getDefault()
		);
		
		ObjMask om = omWithProps.getMask();
		
		VoxelBox<ByteBuffer> vbOutline = calcSurfaceSize( om, !suppressZ );
		
		
		Extent extnt = om.getBoundingBox().extnt();
		
		int size = 0;
		
		try {
			for( int z=0; z<extnt.getZ(); z++) {
				VoxelStatistics stats = params.getPxlPartMemo().doOperation().statisticsFor(maskIndex, 0, z);
				if( stats.histogram().hasAboveZero() ) {
					size += vbOutline.extractSlice(z).countEqual( om.getBinaryValues().getOnInt() );
				}
			}
		} catch (ExecuteException | OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
		
		return size;
	}

	public int getMaskIndex() {
		return maskIndex;
	}


	public void setMaskIndex(int maskIndex) {
		this.maskIndex = maskIndex;
	}


	public RegionMap getRegionMap() {
		return regionMap;
	}


	public void setRegionMap(RegionMap regionMap) {
		this.regionMap = regionMap;
	}


	public int getRegionID() {
		return regionID;
	}


	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}


	public boolean isSuppressZ() {
		return suppressZ;
	}


	public void setSuppressZ(boolean suppressZ) {
		this.suppressZ = suppressZ;
	}

}
