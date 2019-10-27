package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpair;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.orientation.DirectionVector;

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
	public double calcCast(FeatureObjMaskPairParams params)
			throws FeatureCalcException {

	
		Point3d pnt1 = params.getObjMask1().centerOfGravity();
		Point3d pnt2 = params.getObjMask2().centerOfGravity();
		
		if (suppressZ) {
			pnt1.setZ(0);
			pnt2.setZ(0);
		}
		
		// We measure the euclidian distance between centre-points
		DirectionVector vec = DirectionVector.createBetweenTwoPoints( pnt1, pnt2 );
		double maxDist = maxDistance.rslv(params.getRes(), vec );
		
		double dist = pnt1.distance(pnt2);
		
		//System.out.printf("Distance %f\n", dist);
		
		if (dist>maxDist) {
			return 1.0;
		}
				
		double overlapRatio = OverlapRelativeToMaxVolume.calcOverlapRatio(params.getObjMask1(), params.getObjMask2());
		
		if (overlapRatio>minOverlap) {
			return 1.0 - overlapRatio;
		} else {
			return 1.0;
		}
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
