package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.anchoranalysis.plugin.image.feature.object.calculation.single.moment.CalculateObjMaskPointsSecondMomentMatrix;

public abstract class AxisMomentsBase extends FeatureSingleObject {

	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressZCovariance = false;		// Supresses covariance in the z-direction.
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		
		FeatureInputSingleObject params = input.get();
		
		// Max intensity projection of the input mask
		ObjectMask om = params.getObjMask();

		// If we have these few pixels, assume we are perfectly ellipsoid
		if (om.numPixelsLessThan(12)) {
			return 1.0;
		}
		
		MomentsFromPointsCalculator moments = calcMoments(input);
		return calcFromMoments(moments);
	}
	
	protected abstract double calcFromMoments( MomentsFromPointsCalculator moments ) throws FeatureCalcException;
	
	private MomentsFromPointsCalculator calcMoments(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		MomentsFromPointsCalculator moments = input.calc(
			new CalculateObjMaskPointsSecondMomentMatrix(suppressZCovariance)	
		);
		
		moments = moments.duplicate();
		
		// Disconsider the z moment
		moments.removeClosestToUnitZ();
		return moments;
	}
		
	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}

	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}
}
