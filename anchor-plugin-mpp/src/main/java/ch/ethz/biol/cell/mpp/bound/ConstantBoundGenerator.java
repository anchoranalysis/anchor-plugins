package ch.ethz.biol.cell.mpp.bound;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;


/*
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class ConstantBoundGenerator extends BoundCalculator {

	// START BEAN PROPERTIES
	@BeanField
	private RslvdBound constantBound;
	// END BEAN PROPERTIES
	
	public ConstantBoundGenerator() {
		// Standardbean constructor
	}
	
	public ConstantBoundGenerator(RslvdBound constantBound) {
		super();
		this.constantBound = constantBound;
	}
	
	@Override
	public BidirectionalBound calcBound(Point3d point, RotationMatrix rotMatrix) {
		BidirectionalBound bib = new BidirectionalBound();
		bib.setForward( constantBound );
		bib.setReverse( constantBound );
		return bib;
	}

	@Override
	public boolean paramsEquals(Object other) {
		// TODO Auto-generated method stub
		return false;
	}
}
