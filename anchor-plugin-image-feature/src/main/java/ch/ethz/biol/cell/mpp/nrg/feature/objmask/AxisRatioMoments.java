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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.anchoranalysis.points.moment.CalculateObjMaskPointsSecondMomentMatrix;

public class AxisRatioMoments extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressZCovariance = false;		// Supresses covariance in the z-direction.
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> paramsCacheable) throws FeatureCalcException {
		
		FeatureInputSingleObj params = paramsCacheable.getParams();
		
		// Max intensity projection of the input mask
		ObjMask om = params.getObjMask();

		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(12)) {
			return 1.0;
		}

		MomentsFromPointsCalculator moments = paramsCacheable.calc(
			new CalculateObjMaskPointsSecondMomentMatrix(suppressZCovariance)	
		);
		
		moments = moments.duplicate();
		moments.removeClosestToUnitZ();
		// Disconsider the z moment

		double moments0 = moments.get(0).eigenvalueNormalizedAsAxisLength();
		double moments1 = moments.get(1).eigenvalueNormalizedAsAxisLength();
		
		if (moments0==0.0 && moments1==0.0) {
			return 1.0;
		}
		
		if (moments0==0.0 || moments1==0.0) {
			throw new FeatureCalcException("All moments are 0");
		}
		
		double ratio = moments0/moments1;
		//assert( !Double.isNaN(ratio) );
		return ratio;
	}

	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}
}
