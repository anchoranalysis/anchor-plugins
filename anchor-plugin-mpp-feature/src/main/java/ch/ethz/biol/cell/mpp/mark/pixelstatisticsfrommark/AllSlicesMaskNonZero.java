package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

// Gets all slices where indexNonZero has at least one non-zero pixel in that slice 
public class AllSlicesMaskNonZero extends PixelStatisticsFromMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3617915321417174160L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	
	@BeanField
	private int indexNonZero = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int sliceID = -1;	// -1 indicates that we use all slices
	// END BEAN PROPERTIES
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {
		
		PxlMark pm;
		try {
			pm = pmm.doOperation();
		} catch (ExecuteException e) {
			throw new CreateException(e);
		}
		
		return pm.statisticsForAllSlicesMaskSlice(index, regionID, indexNonZero);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", regionID, index, indexNonZero);
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
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
