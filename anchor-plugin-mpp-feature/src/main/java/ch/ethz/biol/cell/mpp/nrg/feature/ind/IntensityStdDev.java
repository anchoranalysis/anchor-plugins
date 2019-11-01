package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;

import ch.ethz.biol.cell.mpp.nrg.NRGElemInd;
import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParams;

public class IntensityStdDev extends NRGElemInd {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private boolean ignoreZero = false;
	
	@BeanField
	private int emptyValue = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast( NRGElemIndCalcParams params ) throws FeatureCalcException {

		try {
			PxlMark pm = params.getPxlPartMemo().doOperation();
			
			VoxelStatistics stats = pm.statisticsForAllSlices(nrgIndex, regionID);
			
			//PixelList combinedInside = pm.getPartitionList().get(nrgIndex).getPxlListForAllSlicesRO(regionID);
			
			// TODO review the sense of this feature, when dealing with multiple channels
			//return stats.stdDev() / 255;
			
			
			if (ignoreZero) {
				Histogram h = stats.histogram().duplicate();
				h.zeroVal(0);
				
				if (h.getTotalCount()==0) {
					return emptyValue;
				}
				
				VoxelStatistics ps = new VoxelStatisticsFromHistogram(h);
				return ps.stdDev();
			} else {
				if (stats.size()==0) {
					return emptyValue;
				}
				
				return stats.stdDev();
			}
		} catch (ExecuteException | OperationFailedException e) {
			throw new FeatureCalcException(e);
		}							
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public boolean isIgnoreZero() {
		return ignoreZero;
	}

	public void setIgnoreZero(boolean ignoreZero) {
		this.ignoreZero = ignoreZero;
	}

	public int getEmptyValue() {
		return emptyValue;
	}

	public void setEmptyValue(int emptyValue) {
		this.emptyValue = emptyValue;
	}
}
