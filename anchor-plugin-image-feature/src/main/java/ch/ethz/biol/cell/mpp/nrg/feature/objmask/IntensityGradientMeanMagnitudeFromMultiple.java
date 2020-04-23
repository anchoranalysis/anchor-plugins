package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*
 * #%L
 * anchor-plugin-image-feature
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


import java.util.List;

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;

/**
 * Calculates the mean of the intensity-gradient defined by multiple NRG channels in a particular direction
 * 
 * An NRG channel is present for X, Y and optionally Z intensity-gradients.
 * 
 * A constant is subtracted from the NRG channel (all positive) to centre around 0
 * 
 * @author Owen Feehan
 *
 */
public class IntensityGradientMeanMagnitudeFromMultiple extends IntensityGradientBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		
		// Calculate the mean
		double sum = 0.0;

		List<Point3d> pnts = params.calc(
			gradientCalculation()
		);
		
		for( Point3d p : pnts ) {
			// Calculate the norm of the point
			sum += p.l2norm();
		}
		
		double res = sum/pnts.size();
		
		//System.out.printf("IntensityGradientMean cc=%d res=%f\n", getCachedCalculationPoints().hashCode(), res );
		
		return res;
	}

}
