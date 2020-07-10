package org.anchoranalysis.plugin.mpp.bean.mark.region;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class MaxNonZero extends IndexedRegionBase {

	@Override
	protected VoxelStatistics createStatisticsFor(VoxelizedMark voxelizedMark, Mark mark, ImageDimensions dim) throws CreateException {

		RelationToThreshold nonZero = new RelationToConstant(
			new GreaterThanBean(),
			0
		);
		
		long maxNonZero = -1;
		VoxelStatistics maxStats = null;
		
		for( int z=0; z<voxelizedMark.getBoundingBox().extent().getZ(); z++ ) {
			VoxelStatistics stats = sliceStatisticsForRegion(voxelizedMark, z);
			
			Histogram h;
			try {
				h = stats.histogram();
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
			
			long num = h.countThreshold(nonZero);
			
			if (num>maxNonZero) {
				maxNonZero = num;
				maxStats = stats;
			}
		}
		
		return maxStats;
	}
}
