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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Specifies a constant if a histogram is empty, otherwise delegates to another {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 * @author Owen Feehan
 *
 */
public class EmptyHistogramConstant extends CalculateLevelOne {

	// START BEAN PROPERTIES
	@BeanField
	private int value = 0;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {
		
		if (!h.isEmpty()) {
			return calculateLevelIncoming(h);
		} else {
			return value;
		}
	}
	
	public int getValue() {
		return value;
	}


	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + value;
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
		EmptyHistogramConstant other = (EmptyHistogramConstant) obj;
		if (value != other.value)
			return false;
		return true;
	}
}
