package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

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
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.EigenvalueAndVector;

/**
 * An element from orientation of a principal-axis (as defined by Image Moments).
 * 
 * @author Owen Feehan
 *
 */
public class PrincipalAxisOrientation extends SpecificAxisBase {

	// START BEAN PROPERTIES
	/** Which axis to read from (x,y,z) */
	@BeanField
	private String axis = "x";
	// END BEAN PROPERTIES

	@Override
	protected double calcFeatureResultFromSpecificMoment(EigenvalueAndVector moment) throws FeatureCalcException {
		int axisIndex = AxisTypeConverter.dimensionIndexFor(
			AxisTypeConverter.createFromString(axis)
		);
		return moment.getEigenvector().get(axisIndex);
	}

	@Override
	protected double resultIfTooFewPixels() throws FeatureCalcException {
		throw new FeatureCalcException("Too few voxels to determine axis-orientation");
	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}

}