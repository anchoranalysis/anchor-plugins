package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;


/*
 * #%L
 * anchor-mpp
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
import org.anchoranalysis.bean.annotation.Positive;

public abstract class ConicFitterBase extends PointsFitter {

	// START BEAN
	@BeanField @Positive
	private double shellRad;
	
	@BeanField
	private double subtractRadii = 0.0;
	
	@BeanField
	private double scaleRadii = 1.0;
	
	@BeanField
	private float inputPointShift = 0.0f;
	// END BEAN
	
	public void duplicateHelper( ConicFitterBase out ) {
		out.shellRad = shellRad;
		out.subtractRadii = subtractRadii;
		out.inputPointShift = inputPointShift;
		out.scaleRadii = scaleRadii;
	}
	
	
	public double getShellRad() {
		return shellRad;
	}

	public void setShellRad(double shellRad) {
		this.shellRad = shellRad;
	}
	
	
	public double getSubtractRadii() {
		return subtractRadii;
	}

	public void setSubtractRadii(double subtractRadii) {
		this.subtractRadii = subtractRadii;
	}
	

	public float getInputPointShift() {
		return inputPointShift;
	}

	public void setInputPointShift(float inputPointShift) {
		this.inputPointShift = inputPointShift;
	}


	public double getScaleRadii() {
		return scaleRadii;
	}


	public void setScaleRadii(double scaleRadii) {
		this.scaleRadii = scaleRadii;
	}
}
