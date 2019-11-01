package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractRadii;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

import ch.ethz.biol.cell.core.CheckMark;

public class RadiiMinRatio extends CheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4380227015245049115L;

	// START BEAN PROPERTIES
	@BeanField
	private double min=1;
	// END BEAN PROPERTIES

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractRadii;
	}

	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {
		
		MarkAbstractRadii markCast = (MarkAbstractRadii) mark;
		double[] radiiOrdered = markCast.radiiOrderedRslvd( nrgStack.getDimensions().getRes() );
		
		int len = radiiOrdered.length;
		assert(len>=2);
		
		assert( radiiOrdered[1] > radiiOrdered[0] );
		if (len==3) {
			assert( radiiOrdered[2] > radiiOrdered[1] );
		}
		
		double ratio = radiiOrdered[len-1] / radiiOrdered[0];
		ratio = Math.max( ratio, 1 / ratio );
		
		return (ratio >= min);
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	@Override
	public FeatureList orderedListOfFeatures() throws CreateException {
		return null;
	}

}
