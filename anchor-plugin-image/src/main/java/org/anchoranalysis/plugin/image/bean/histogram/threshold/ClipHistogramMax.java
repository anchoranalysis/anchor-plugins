package org.anchoranalysis.plugin.image.bean.histogram.threshold;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;


/**
 * Clips the input-histogram to a certain maximum value, and then delegates the calculate-level.
 * 
 * @author owen
 *
 */
public class ClipHistogramMax extends CalculateLevelOne {

	// START BEAN
	@BeanField
	private int max;
	// END BEAN
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {
		Histogram hClipped = createClipped(h,max);
		return calculateLevelIncoming(hClipped);
	}
	
	private static Histogram createClipped( Histogram histIn, int maxVal ) {
		
		assert( maxVal<= histIn.getMaxBin() );
		
		long numAbove = histIn.countThreshold(
			new RelationToConstant(
				new GreaterThanBean(),
				maxVal
			)
		);
		
		Histogram out = new HistogramArray(histIn.getMaxBin());
		for( int i=histIn.getMinBin(); i<=maxVal; i++ ) {
			out.incrValBy( i, histIn.getCount(i) );
		}
		out.incrValBy(maxVal, numAbove);
		return out;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + max;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClipHistogramMax other = (ClipHistogramMax) obj;
		if (max != other.max)
			return false;
		return true;
	}
}
