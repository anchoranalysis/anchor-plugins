package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.mark.Mark;

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


import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMap;

public class FeatureValueMarkGreaterEqualThan extends FeatureValueCheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1086432241915811567L;
	
	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {
		
		if (session==null) {
			throw new CheckException("No session initialized");
		}
		
		try {
			double nrg = session.calc( mark ).get(0);
			
			if (nrg >= minVal) {
				return true;
			} else {
				return false;
			}
			
		} catch (FeatureCalcException e) {
			throw new CheckException(
				"Error calculating feature",
				e
			);
		}
	}
}
