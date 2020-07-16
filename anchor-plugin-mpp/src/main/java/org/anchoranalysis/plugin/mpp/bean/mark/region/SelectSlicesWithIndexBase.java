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
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = false)
public abstract class SelectSlicesWithIndexBase extends SelectSlicesBase {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private int indexNonZero = 0;
	// END BEAN PROPERTIES
	
	@Override
	protected VoxelStatistics createStatisticsFor(VoxelizedMark pm, Mark mark, ImageDimensions dimensions) throws CreateException {
		return extractFromPxlMark(pm);
	}
	
	protected abstract VoxelStatistics extractFromPxlMark( VoxelizedMark pm ) throws CreateException;

	protected VoxelStatistics statisticsForAllSlices( VoxelizedMark pm, boolean useNonZeroIndex ) {
		return pm.statisticsForAllSlicesMaskSlice(
			useNonZeroIndex ? indexNonZero : getIndex(),
			getRegionID(),
			indexNonZero
		);
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
}
