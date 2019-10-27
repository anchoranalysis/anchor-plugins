package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*
 * #%L
 * anchor-plugin-points
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.CalculateEllipsoidLeastSquares;

import ch.ethz.biol.cell.mpp.mark.MarkEllipsoid;

public class Ellipsoidicity extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressZCovariance = false;		// Supresses covariance in the z-direction.
	// END BEAN PROPERTIES
	
	private CachedCalculation<MarkEllipsoid> cc;
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		cc = CalculateEllipsoidLeastSquares.createFromCache(cache,suppressZCovariance);
	}
	
	@Override
	public double calcCast(FeatureObjMaskParams params) throws FeatureCalcException {
		
		// Max intensity projection of the input mask
		ObjMask om = params.getObjMask();
		
		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(12)) {
			return 1.0;
		}
		
		try {
			MarkEllipsoid me = cc.getOrCalculate(params);
					
			return EllipticityCalculatorHelper.calc(
				om,
				me,
				params.getNrgStack().getDimensions()
			);
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}

	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}
}
