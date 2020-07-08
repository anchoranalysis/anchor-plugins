package org.anchoranalysis.plugin.mpp.bean.mark.region;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class SelectSlicesWithIndexBase extends SelectSlicesBase {

	// START BEAN PROPERTIES
	@BeanField
	private int indexNonZero = 0;
	// END BEAN PROPERTIES
	
	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDimensions dim) throws CreateException {
		return extractFromPxlMark(pm);
	}
	
	protected abstract VoxelStatistics extractFromPxlMark( PxlMark pm ) throws CreateException;

	protected VoxelStatistics statisticsForAllSlices( PxlMark pm, boolean useNonZeroIndex ) {
		return pm.statisticsForAllSlicesMaskSlice(
			useNonZeroIndex ? indexNonZero : getIndex(),
			getRegionID(),
			indexNonZero
		);
	}
	
	public int getIndexNonZero() {
		return indexNonZero;
	}

	public void setIndexNonZero(int indexNonZero) {
		this.indexNonZero = indexNonZero;
	}
	
	@Override
	public String uniqueName() {
		return String.format(
			"%s_%d",
			super.uniqueName(),
			indexNonZero
		);
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", getRegionID(), getIndex(), indexNonZero);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + indexNonZero;
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
		SelectSlicesWithIndexBase other = (SelectSlicesWithIndexBase) obj;
		if (indexNonZero != other.indexNonZero)
			return false;
		return true;
	}
}
