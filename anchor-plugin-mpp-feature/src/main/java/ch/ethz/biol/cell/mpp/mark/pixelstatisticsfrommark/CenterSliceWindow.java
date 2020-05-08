package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;


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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsCombined;

/**
 * Like {#link ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.CenterSlice} but considers more than one slice, specifically centerSlice+- windowSize
 * 
 * <p>So total size =  2*windowSize + 1 (clipped to the bounding box)</p>
 * 
 * @author owen
 *
 */
public class CenterSliceWindow extends CenterSliceBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3617915321417174160L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int windowSize = 0;
	// END BEAN PROPERTIES
	
	@Override
	protected VoxelStatistics createStatisticsForBBox(PxlMark pm, ImageDim dim, BoundingBox bbox, int zCenter) {
		
		// If our z-center is off scene we bring it to the closest value, but we guard against the case where the top of the mark is also off scene
		if (zCenter < 0) {
			zCenter = 0;
		}
		
		if (zCenter >= bbox.extnt().getZ()) {
			zCenter = bbox.extnt().getZ()- 1;
		}
		assert(zCenter>=0);
		assert(zCenter<bbox.extnt().getZ());
		
		// Early exit if the windowSize is 0
		if (windowSize==0) {
			return sliceStatisticsForRegion(pm, zCenter);
		}
		
		int zLow = Math.max( zCenter - windowSize, 0);
		int zHigh = Math.min( zCenter + windowSize, bbox.extnt().getZ()- 1);
		
		VoxelStatisticsCombined out = new VoxelStatisticsCombined();
		for (int z=zLow; z<=zHigh; z++) {
			out.add(
				sliceStatisticsForRegion(pm, z)
			);
		}
		return out;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + windowSize;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CenterSliceWindow other = (CenterSliceWindow) obj;
		if (windowSize != other.windowSize)
			return false;
		return true;
	}

	@Override
	public String uniqueName() {
		return super.uniqueName() + "_" + windowSize;
	}
}
