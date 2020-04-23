package org.anchoranalysis.plugin.image.feature.bean.obj.pair.overlap;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.plugin.image.feature.obj.pair.overlap.OverlapRatioUtilities;

public class CostOverlapWithinMidpointDistance extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance maxDistance;
	
	@BeanField
	private double minOverlap = 0.6;
	
	@BeanField
	private boolean suppressZ = true;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureInputPairObjs> paramsCacheable)
			throws FeatureCalcException {

		FeatureInputPairObjs params = paramsCacheable.getParams();
		
		if (isDistMoreThanMax(params)) {
			return 1.0;
		}
				
		double overlapRatio = OverlapRatioUtilities.overlapRatioToMaxVolume(params);
		
		if (overlapRatio>minOverlap) {
			return 1.0 - overlapRatio;
		} else {
			return 1.0;
		}
	}
	
	private boolean isDistMoreThanMax( FeatureInputPairObjs params ) {
		
		Point3d cog1 = params.getObjMask1().centerOfGravity();
		Point3d cog2 = params.getObjMask2().centerOfGravity();
		
		double dist = calcDist(cog1, cog2);
		double maxDist = calcMaxDist(cog1, cog2, params.getRes());
		
		return dist > maxDist;
	}
	
	private double calcDist( Point3d cog1, Point3d cog2 ) {
		if (suppressZ) {
			cog1.setZ(0);
			cog2.setZ(0);
		}
		return cog1.distance(cog2);
	}

	// We measure the euclidian distance between centre-points
	private double calcMaxDist( Point3d cog1, Point3d cog2, ImageRes res ) {
		DirectionVector vec = DirectionVector.createBetweenTwoPoints( cog1, cog2 );
		 return maxDistance.rslv(res, vec );
	}

	public UnitValueDistance getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(UnitValueDistance maxDistance) {
		this.maxDistance = maxDistance;
	}

	public double getMinOverlap() {
		return minOverlap;
	}

	public void setMinOverlap(double minOverlap) {
		this.minOverlap = minOverlap;
	}

	public boolean isSuppressZ() {
		return suppressZ;
	}

	public void setSuppressZ(boolean suppressZ) {
		this.suppressZ = suppressZ;
	}

}
