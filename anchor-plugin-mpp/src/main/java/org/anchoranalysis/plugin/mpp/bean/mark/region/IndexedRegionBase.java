package org.anchoranalysis.plugin.mpp.bean.mark.region;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkRegion;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

/**
 * {@link MarkRegion} with a region and an index
 * 
 * @author Owen Feehan
 *
 */
public abstract class IndexedRegionBase extends MarkRegion {

	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d", regionID, index);
	}
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDimensions dim) throws CreateException {
		PxlMark pm = pmm.doOperation();
		return createStatisticsFor(pm, pmm.getMark(), dim);
	}
	
	protected abstract VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDimensions dim) throws CreateException;
	
	protected VoxelStatistics sliceStatisticsForRegion(PxlMark pm, int z) {
		return pm.statisticsFor(index, regionID, z);
	}
	
	protected BoundingBox boundingBoxForRegion( PxlMark pm ) {
		return pm.getBoundingBox(regionID);
	}
			
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + regionID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexedRegionBase other = (IndexedRegionBase) obj;
		if (index != other.index)
			return false;
		if (regionID != other.regionID)
			return false;
		return true;
	}

	@Override
	public String uniqueName() {
		return String.format(
			"%s_%d_%d",
			getClass().getCanonicalName(),
			index,
			regionID
		);
	}
}
