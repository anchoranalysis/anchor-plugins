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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.GreaterThan;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;

import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;
import ch.ethz.biol.cell.mpp.mark.pxlmark.PxlMark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

// Only takes pixels where indexNonZero has a nonzero pixel 
// This involves a trick where we count how many pixels exist
//  in our mask and we take the highest num-pixels to match this
//  from our initial histogram
public class AllSlicesMaskEverythingNonZero extends PixelStatisticsFromMark {

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
		
		Histogram histIndex;
		Histogram histNonZero;
		try {
			histIndex = pm.statisticsForAllSlicesMaskSlice(index, regionID, indexNonZero).histogram();
			histNonZero = pm.statisticsForAllSlicesMaskSlice(indexNonZero, regionID, indexNonZero).histogram();
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		
		long numNonZero = histNonZero.countThreshold( new GreaterThan(), 0 );
		
		Histogram hOut = histIndex.duplicate();
		hOut = hOut.extractPixelsFromRight(numNonZero);
		return new VoxelStatisticsFromHistogram(hOut);
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
