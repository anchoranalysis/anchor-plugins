package org.anchoranalysis.plugin.points.calculate.ellipsoid;

/*
 * #%L
 * anchor-plugin-points
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


import java.util.List;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.plugin.points.calculate.CalculatePntsFromOutline;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateEllipsoidLeastSquares extends FeatureCalculation<MarkEllipsoid,FeatureInputSingleObj> {

	private boolean suppressZCovariance;
	
	private transient ResolvedCalculation<List<Point3i>,FeatureInputSingleObj> ccPnts;
		
	private CalculateEllipsoidLeastSquares(boolean suppressZCovariance, ResolvedCalculation<List<Point3i>,FeatureInputSingleObj> ccPnts) {
		super();
		this.suppressZCovariance = suppressZCovariance;
		this.ccPnts = ccPnts;
	}
	
	public static MarkEllipsoid createFromCache(SessionInput<FeatureInputSingleObj> input, boolean suppressZCovariance ) throws FeatureCalcException {
		
		ResolvedCalculation<List<Point3i>,FeatureInputSingleObj> ccPnts = input.resolver().search( new CalculatePntsFromOutline() );
		
		ResolvedCalculation<MarkEllipsoid,FeatureInputSingleObj> ccEllipsoid = input.resolver().search(
			new CalculateEllipsoidLeastSquares(suppressZCovariance, ccPnts )
		);
		return input.calc(ccEllipsoid);
	}
	
	@Override
	protected MarkEllipsoid execute( FeatureInputSingleObj input ) throws FeatureCalcException {
		
		try {
			// Shell Rad is arbitrary here for now
			return EllipsoidFactory.createMarkEllipsoidLeastSquares(
				new CachedCalculationOperation<>(ccPnts,input),
				input.getDimensionsRequired(),
				suppressZCovariance,
				0.2
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateEllipsoidLeastSquares){
	        final CalculateEllipsoidLeastSquares other = (CalculateEllipsoidLeastSquares) obj;
	        return new EqualsBuilder()
	            .append(suppressZCovariance, other.suppressZCovariance)
	            .append(ccPnts, other.ccPnts)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(suppressZCovariance).append(ccPnts).toHashCode();
	}
}
