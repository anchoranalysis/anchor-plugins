package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*-
 * #%L
 * anchor-plugin-points
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

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.points.calculate.ellipsoid.CalculateEllipsoidLeastSquares;

import lombok.Getter;
import lombok.Setter;

public abstract class EllipsoidBase extends FeatureSingleObject {

	// START BEAN PROPERTIES
	/** Iff true, supresses covariance in the z-direction. */
	@BeanField @Getter @Setter
	private boolean suppressZ = false;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		
		ObjectMask object = input.get().getObject();
		
		// If we have these few pixels, assume we are perfectly ellipsoid
		if (object.numPixelsLessThan(12)) {
			return 1.0;
		}
		
		MarkEllipsoid me = CalculateEllipsoidLeastSquares.createFromCache(
			input,
			suppressZ
		);
		
		return calc(input.get(), me);
	}
	
	protected abstract double calc(FeatureInputSingleObject input, MarkEllipsoid me) throws FeatureCalcException;
}
