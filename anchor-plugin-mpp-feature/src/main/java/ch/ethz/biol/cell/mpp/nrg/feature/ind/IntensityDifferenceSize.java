package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
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
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class IntensityDifferenceSize extends FeatureSingleMemo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField @NonNegative
	private double minDiff;
	// END BEAN PROPERTIES
	
	public IntensityDifferenceSize() {
		super();
	}
	
	
	public IntensityDifferenceSize(double minDiff) {
		super();
		this.minDiff = minDiff;
	}
	
	@Override
	public double calcCast( FeatureInputSingleMemo params ) throws FeatureCalcException {

		PxlMark pm = params.getPxlPartMemo().doOperation();
		
		VoxelStatistics statsInside = pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_INSIDE);
		VoxelStatistics statsOutside = pm.statisticsForAllSlices(0, GlobalRegionIdentifiers.SUBMARK_SHELL);
		
		double mean_in = statsInside.mean();
		double mean_shell = statsOutside.mean();
		
		double diff = ( ((mean_in-mean_shell) - minDiff) / 255 );
		long size = statsInside.size();
		
		return (diff * size) * 1e-3;
	}

	@Override
	public String getParamDscr() {
		return String.format("minDiff=%f", minDiff);
	}

	public double getMinDiff() {
		return minDiff;
	}

	public void setMinDiff(double minDiff) {
		this.minDiff = minDiff;
	}
}
