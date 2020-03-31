package org.anchoranalysis.plugin.image.bean.threshold.calculatelevel;

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
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class IfGreaterThan extends CalculateLevel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevel;		// Prior comes from here always
	
	@BeanField
	private CalculateLevel calculateLevelElse;
	
	@BeanField
	private int threshold;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {

		int level = calculateLevel.calculateLevel(h);
		if (level>threshold) {
			return calculateLevelElse.calculateLevel(h);
		} else {
			return level;
		}
	}
	
	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}
	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}
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
	public boolean equals(Object obj) {
		if(obj instanceof IfGreaterThan){
	    	final IfGreaterThan other = (IfGreaterThan) obj;
	        return new EqualsBuilder()
	            .append(calculateLevel, other.calculateLevel)
	            .append(calculateLevelElse, other.calculateLevelElse)
	            .append(threshold, other.threshold)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(calculateLevel)
			.append(calculateLevelElse)
			.append(threshold)
			.toHashCode();
	}
}
