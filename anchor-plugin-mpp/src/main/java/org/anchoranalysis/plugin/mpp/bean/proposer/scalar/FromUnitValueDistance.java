package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageRes;

public class FromUnitValueDistance extends ScalarProposer {

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance unitValueDistance;
	
	@BeanField
	private DirectionVectorBean directionVector;
	// END BEAN PROPERTIES
	
	@Override
	public double propose(RandomNumberGenerator re, ImageRes res)
			throws OperationFailedException {
		// TODO this could be a bit slow, we are creating an object on the heap every time from directionVector
		return unitValueDistance.rslv(res, directionVector.createVector());
	}

	public UnitValueDistance getUnitValueDistance() {
		return unitValueDistance;
	}

	public void setUnitValueDistance(UnitValueDistance unitValueDistance) {
		this.unitValueDistance = unitValueDistance;
	}

	public DirectionVectorBean getDirectionVector() {
		return directionVector;
	}

	public void setDirectionVector(DirectionVectorBean directionVector) {
		this.directionVector = directionVector;
	}




}
