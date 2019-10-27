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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.plugin.points.calculate.ellipse.CalculateEllipseLeastSquares;
import org.anchoranalysis.plugin.points.calculate.ellipse.ObjMaskAndEllipse;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.InsufficientPointsException;

// Calculates the ellipticity of a objMask (on the COG slice if it's a zstack)
public class Ellipticity extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private CachedCalculation<ObjMaskAndEllipse> cc;

	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession session)
			throws InitException {
		super.beforeCalc(params, session);
		cc = session.search( new CalculateEllipseLeastSquares() );
	}
	
	@Override
	public double calcCast(FeatureObjMaskParams params) throws FeatureCalcException {
		

		ObjMaskAndEllipse both;
		try {
			both = cc.getOrCalculate(params);
		} catch (ExecuteException e) {
			if (e.getCause() instanceof InsufficientPointsException) {
				// If we don't have enough points, we return perfectly ellipticity as it's so small
				return 1.0;
			} else {
				throw new FeatureCalcException(e);
			}
		}
		
		ObjMask om = both.getObjMask();
		
		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(6)) {
			return 1.0;
		}

		return EllipticityCalculatorHelper.calc(
			om,
			both.getMark(),
			params.getNrgStack().getDimensions()
		);
	}
}
