package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

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

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class AllSlicesBase extends IndexedRegionBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int indexNonZero = 0;
	
	@BeanField
	private int sliceID = -1;	// -1 indicates that we use all slices
	// END BEAN PROPERTIES

	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDim dim) throws CreateException {
		return extractFromPxlMark(pm);
	}
	
	protected abstract VoxelStatistics extractFromPxlMark( PxlMark pm ) throws CreateException;

	protected VoxelStatistics statisticsForAllSlices( PxlMark pm, boolean useNonZeroIndex ) throws CreateException {
		return pm.statisticsForAllSlicesMaskSlice(
			useNonZeroIndex ? indexNonZero : getIndex(),
			getRegionID(),
			indexNonZero
		);
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", getRegionID(), getIndex(), indexNonZero);
	}

	public int getSliceID() {
		return sliceID;
	}

	public void setSliceID(int sliceID) {
		this.sliceID = sliceID;
	}

	public int getIndexNonZero() {
		return indexNonZero;
	}

	public void setIndexNonZero(int indexNonZero) {
		this.indexNonZero = indexNonZero;
	}
}
