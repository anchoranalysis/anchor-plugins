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
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Specifies a constant if a histogram is empty, otherwise delegates to another {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 * @author owen
 *
 */
public class EmptyHistogramConstant extends CalculateLevel {

	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField
	private int value = 0;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {
		
		if (!h.isEmpty()) {
			return calculateLevel.calculateLevel(h);
		} else {
			return value;
		}
	}

	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}


	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}


	public int getValue() {
		return value;
	}


	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EmptyHistogramConstant){
	    	final EmptyHistogramConstant other = (EmptyHistogramConstant) obj;
	        return new EqualsBuilder()
	            .append(calculateLevel, other.calculateLevel)
	            .append(value, other.value)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(calculateLevel)
			.append(value)
			.toHashCode();
	}
}
