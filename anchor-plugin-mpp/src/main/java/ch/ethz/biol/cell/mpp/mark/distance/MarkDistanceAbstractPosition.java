package ch.ethz.biol.cell.mpp.mark.distance;

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


import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.MarkAbstractPosition;
import ch.ethz.biol.cell.mpp.mark.UnsupportedMarkTypeException;

public class MarkDistanceAbstractPosition extends MarkDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1919575942780482503L;

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}

	@Override
	public double distance(Mark mark1, Mark mark2) throws UnsupportedMarkTypeException {
		
		if (!(mark1 instanceof MarkAbstractPosition)) {
			throw new UnsupportedMarkTypeException("mark1 is not MarkAbstractPosition");
		}
		
		if (!(mark2 instanceof MarkAbstractPosition)) {
			throw new UnsupportedMarkTypeException("mark2 is not MarkAbstractPosition");
		}
		
		MarkAbstractPosition mark1Cast = (MarkAbstractPosition) mark1;
		MarkAbstractPosition mark2Cast = (MarkAbstractPosition) mark2;
		return mark1Cast.getPos().distance( mark2Cast.getPos() );
	}
}
	