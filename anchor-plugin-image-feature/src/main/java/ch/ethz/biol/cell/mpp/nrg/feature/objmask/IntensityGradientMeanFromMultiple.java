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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeUtilities;
import org.anchoranalysis.core.cache.ExecuteException;
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
public class IntensityGradientMeanFromMultiple extends IntensityGradientBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private String axis="";
	// END BEAN PROPERTIES
		
	@Override
	public double calcCast(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		
		AxisType axisType = AxisTypeUtilities.createFromString( axis );
		
		// Calculate the mean
		double sum = 0.0;
		try {
			List<Point3d> pnts = params.calc(
				gradientCalculation()
			);
			
			for( Point3d p : pnts ) {
				sum += p.get(axisType);
			}
			
			return sum/pnts.size();
			
		} catch (ExecuteException e) {
			throw new FeatureCalcException( e.getCause() );
		}
	}

	public String getAxis() {
		return axis;
	}


	public void setAxis(String axis) {
		this.axis = axis;
	}


}
