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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationOperation;
import org.anchoranalysis.feature.session.cache.ICachedCalculationSearch;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.plugin.points.calculate.CalculatePntsFromOutline;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CalculateEllipsoidLeastSquares extends CachedCalculationCastParams<MarkEllipsoid,FeatureObjMaskParams> {

	private boolean suppressZCovariance;
	
	private transient CachedCalculation<List<Point3i>> ccPnts;
		
	private CalculateEllipsoidLeastSquares(boolean suppressZCovariance, CachedCalculation<List<Point3i>> ccPnts) {
		super();
		this.suppressZCovariance = suppressZCovariance;
		this.ccPnts = ccPnts;
	}
	
	public static CachedCalculation<MarkEllipsoid> createFromCache( ICachedCalculationSearch cache, boolean suppressZCovariance ) {
		CachedCalculation<List<Point3i>> ccPnts = cache.search( new CalculatePntsFromOutline() );
		return cache.search( new CalculateEllipsoidLeastSquares(suppressZCovariance, ccPnts ) );
	}
	
	@Override
	protected MarkEllipsoid execute( FeatureObjMaskParams params ) throws ExecuteException {
		
		try {
			// Shell Rad is arbitrary here for now
			return EllipsoidFactory.createMarkEllipsoidLeastSquares(
				new CachedCalculationOperation<List<Point3i>>(ccPnts,params),
				params.getNrgStack().getDimensions(),
				suppressZCovariance,
				0.2
			);
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}
	}

	
	@Override
	public CalculateEllipsoidLeastSquares duplicate() {
		return new CalculateEllipsoidLeastSquares(suppressZCovariance,ccPnts.duplicate());
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
