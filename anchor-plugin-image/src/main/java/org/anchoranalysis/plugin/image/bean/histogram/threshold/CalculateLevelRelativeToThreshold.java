package org.anchoranalysis.plugin.image.bean.histogram.threshold;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class CalculateLevelRelativeToThreshold extends CalculateLevelOne {

	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevelElse;
	
	@BeanField
	private int threshold;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {

		int level = calculateLevelIncoming(h);
		if (useElseInstead(level,threshold)) {
			return calculateLevelElse.calculateLevel(h);
		} else {
			return level;
		}
	}
	
	/** Uses the {@link calculateLevelElse} instead of {@link calculateLevel} */
	protected abstract boolean useElseInstead(int level, int threshold);
	
	public CalculateLevel getCalculateLevelElse() {
		return calculateLevelElse;
	}
	public void setCalculateLevelElse(CalculateLevel calculateLevelElse) {
		this.calculateLevelElse = calculateLevelElse;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((calculateLevelElse == null) ? 0 : calculateLevelElse.hashCode());
		result = prime * result + threshold;
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
		CalculateLevelRelativeToThreshold other = (CalculateLevelRelativeToThreshold) obj;
		if (calculateLevelElse == null) {
			if (other.calculateLevelElse != null)
				return false;
		} else if (!calculateLevelElse.equals(other.calculateLevelElse))
			return false;
		if (threshold != other.threshold)
			return false;
		return true;
	}
}
