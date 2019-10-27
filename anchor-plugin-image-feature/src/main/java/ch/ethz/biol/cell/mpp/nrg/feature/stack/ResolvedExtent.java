package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;


public class ResolvedExtent extends FeatureStackFromDimensions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PARAMETERS
	@BeanField
	private String axis = "x";
	// END BEAN PARAMETERS

	@Override
	protected double calcFromDims(ImageDim dim) {
		String axisLowerCase = axis.toLowerCase();
		
		return calcAxisValue(axisLowerCase, dim.getRes() );
	}
	
	private double calcAxisValue( String axisLowerCase, ImageRes sr ) {
		
		if (axisLowerCase.equals("x")) {
			return sr.getX();
		} else if (axisLowerCase.equals("y")) {
			return sr.getY();
		} else if (axisLowerCase.equals("z")) {
			return sr.getZ();
		} else {
			assert false;
			return -1;
		}
	}
	
	
	@Override
	public String getParamDscr() {
		return String.format("%s", axis);
	}

	public String getAxis() {
		return axis;
	}

	public void setAxis(String axis) {
		this.axis = axis;
	}
}
