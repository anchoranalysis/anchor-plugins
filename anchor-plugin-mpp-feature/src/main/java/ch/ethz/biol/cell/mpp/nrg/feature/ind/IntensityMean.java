package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.PixelStatisticsFromMark;

public class IntensityMean extends FeatureSingleMemo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN
	@BeanField
	private PixelStatisticsFromMark pixelList;
	
	@BeanField
	private boolean ignoreZero = false;
	
	@BeanField
	private double emptyValue = 0;
	// END BEAN
	
	@Override
	public double calcCast( FeatureInputSingleMemo params ) throws FeatureCalcException {

		try {
			VoxelStatistics stats = pixelList.createStatisticsFor(params.getPxlPartMemo(), params.getDimensions() );
			
			if (stats.size()==0) {
				return emptyValue;
			}

			if (ignoreZero) {
				Histogram h = stats.histogram();
				
				if (!h.hasAboveZero()) {
					return emptyValue;
				}
				
				return h.meanNonZero();
			}
			
			return stats.mean();
		} catch (IndexOutOfBoundsException | CreateException | OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public String getParamDscr() {
		return pixelList.getBeanDscr();
	}
	
	public PixelStatisticsFromMark getPixelList() {
		return pixelList;
	}

	public void setPixelList(PixelStatisticsFromMark pixelList) {
		this.pixelList = pixelList;
	}
	
	public boolean isIgnoreZero() {
		return ignoreZero;
	}


	public void setIgnoreZero(boolean ignoreZero) {
		this.ignoreZero = ignoreZero;
	}


	public double getEmptyValue() {
		return emptyValue;
	}


	public void setEmptyValue(double emptyValue) {
		this.emptyValue = emptyValue;
	}
}
