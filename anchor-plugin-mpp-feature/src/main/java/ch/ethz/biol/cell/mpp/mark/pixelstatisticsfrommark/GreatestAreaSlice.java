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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class GreatestAreaSlice extends PixelStatisticsFromMark {

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
	private RelationBean relationToThreshold;		// Definies what is INSIDE
	
	@BeanField
	private int threshold;
	// END BEAN PROPERTIES

	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {
		
		PxlMark pm;
		try {
			pm = pmm.doOperation();
		} catch (ExecuteException e) {
			throw new CreateException(e);
		}
		
		BoundingBox bbox = pm.getBoundingBox(regionID);

		RelationToValue relation = relationToThreshold.create();
		
		long maxArea = -1;
		VoxelStatistics psMax = null;
		for( int z=0; z<bbox.extnt().getZ(); z++) {
			
			//int z1 = bbox.getCrnrMin().getZ()+z;
			
			VoxelStatistics ps = pm.statisticsFor(index, regionID, z);
			long num = ps.countThreshold(relation, threshold);
			
			if (num>maxArea) {
				psMax = ps;
				maxArea = num;
			}
		}
		
		assert( psMax!=null );
		
		return psMax;
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

	public RelationBean getRelationToThreshold() {
		return relationToThreshold;
	}

	public void setRelationToThreshold(RelationBean relationToThreshold) {
		this.relationToThreshold = relationToThreshold;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}



}
