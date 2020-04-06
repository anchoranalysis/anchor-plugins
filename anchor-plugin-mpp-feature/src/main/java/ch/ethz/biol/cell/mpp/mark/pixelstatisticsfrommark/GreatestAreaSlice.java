package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.Mark;
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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class GreatestAreaSlice extends IndexedRegionBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3617915321417174160L;
	
	// START BEAN PROPERTIES
	@BeanField
	private RelationBean relationToThreshold;		// Definies what is INSIDE
	
	@BeanField
	private int threshold;
	// END BEAN PROPERTIES


	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDim dim) throws CreateException {

		BoundingBox bbox = boundingBoxForRegion(pm);

		RelationToValue relation = relationToThreshold.create();
		
		long maxArea = -1;
		VoxelStatistics psMax = null;
		for( int z=0; z<bbox.extnt().getZ(); z++) {
			
			VoxelStatistics ps = sliceStatisticsForRegion(pm, z);
			long num = ps.countThreshold(relation, threshold);
			
			if (num>maxArea) {
				psMax = ps;
				maxArea = num;
			}
		}
		
		assert( psMax!=null );
		
		return psMax;
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
