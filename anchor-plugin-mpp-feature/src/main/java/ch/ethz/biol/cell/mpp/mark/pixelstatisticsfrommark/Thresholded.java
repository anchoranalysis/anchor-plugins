package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

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
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class Thresholded extends MarkRegion {

	// START BEAN PROPERTIES
	@BeanField
	private MarkRegion region;
	
	@BeanField
	private RelationToThreshold threshold;
	// END BEAN PROPERTIES
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {
		return region.createStatisticsFor(pmm, dim).threshold(threshold);
	}

	public RelationToThreshold getThreshold() {
		return threshold;
	}

	public void setThreshold(RelationToThreshold threshold) {
		this.threshold = threshold;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
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
		Thresholded other = (Thresholded) obj;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (threshold == null) {
			if (other.threshold != null)
				return false;
		} else if (!threshold.equals(other.threshold))
			return false;
		return true;
	}

	@Override
	public String uniqueName() {
		return String.format(
			"%s_%s_%s",
			Thresholded.class.getCanonicalName(),
			region.uniqueName(),
			threshold.uniqueName()
		);
	}

	public MarkRegion getRegion() {
		return region;
	}

	public void setRegion(MarkRegion region) {
		this.region = region;
	}
}
