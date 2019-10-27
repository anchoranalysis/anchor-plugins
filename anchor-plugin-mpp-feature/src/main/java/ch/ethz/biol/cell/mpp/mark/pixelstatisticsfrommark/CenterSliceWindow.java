package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsCombined;

import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.MarkAbstractPosition;
import ch.ethz.biol.cell.mpp.mark.pxlmark.PxlMark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

// Considers the centerSlice +- windowSize
//  So total size =  2*windowSize + 1 (clipped to the bounding box)
public class CenterSliceWindow extends PixelStatisticsFromMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3617915321417174160L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int windowSize = 0;
	// END BEAN PROPERTIES
	
	public static VoxelStatistics createStatisticsFor(PxlMark pm, Mark markUncast, ImageDim dim, int index, int regionID, int windowSize) {
		
		BoundingBox bbox = pm.getBoundingBox(regionID);
		
		MarkAbstractPosition mark = (MarkAbstractPosition) markUncast;
		int zCenter = (int) Math.round(mark.getPos().getZ()) - bbox.getCrnrMin().getZ();
		//int zMax = bbox.calcCrnrMax().getZ();
		
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
			return pm.statisticsFor(index, regionID, zCenter);
		}
		
		int zLow = Math.max( zCenter - windowSize, 0);
		int zHigh = Math.min( zCenter + windowSize, bbox.extnt().getZ()- 1);
		
		VoxelStatisticsCombined out = new VoxelStatisticsCombined();
		for (int z=zLow; z<=zHigh; z++) {
			out.add( pm.statisticsFor(index, regionID, z) );
		}
		return out;
	}
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {
		
		PxlMark pm;
		try {
			pm = pmm.doOperation();
		} catch (ExecuteException e) {
			throw new CreateException(e);
		}
		return createStatisticsFor(pm, pmm.getMark(), dim, index, regionID, windowSize);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d", regionID, index);
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
}
