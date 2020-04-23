package org.anchoranalysis.points.moment;

/*
 * #%L
 * anchor-points
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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateObjMaskPointsSecondMomentMatrix extends CachedCalculation<MomentsFromPointsCalculator,FeatureObjMaskParams> {

	private boolean suppressZ;
		
	public CalculateObjMaskPointsSecondMomentMatrix(boolean suppressZ) {
		super();
		this.suppressZ = suppressZ;
	}

	@Override
	protected MomentsFromPointsCalculator execute( FeatureObjMaskParams params ) throws ExecuteException {
		return MomentsFromObjMask.apply(params.getObjMask(),suppressZ);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(suppressZ).toHashCode();
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateObjMaskPointsSecondMomentMatrix){
	        final CalculateObjMaskPointsSecondMomentMatrix other = (CalculateObjMaskPointsSecondMomentMatrix) obj;
	        return new EqualsBuilder()
	            .append(suppressZ, other.suppressZ)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
}
