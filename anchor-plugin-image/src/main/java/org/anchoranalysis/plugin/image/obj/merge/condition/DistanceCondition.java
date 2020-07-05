package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.BoundingBoxDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

public class DistanceCondition implements BeforeCondition {

	private UnitValueDistance maxDist;
	private boolean suppressZ;
	private LogReporter logReporter;
	
	public DistanceCondition(UnitValueDistance maxDist, boolean suppressZ, LogReporter logReporter) {
		super();
		this.maxDist = maxDist;
		this.suppressZ = suppressZ;
		this.logReporter = logReporter;
	}

	@Override
	public boolean accept(ObjectMask omSrc, ObjectMask omDest, Optional<ImageResolution> res) throws OperationFailedException {
		
		// We impose a max dist condition if necessary
		if (maxDist!=null) {
			return isWithinMaxDist(omSrc,omDest,res);
		} else {
			return true;
		}
	}
	
	private boolean isWithinMaxDist( ObjectMask omSrc, ObjectMask omDest, Optional<ImageResolution> res ) throws OperationFailedException {
		
		double dist = BoundingBoxDistance.distance( omSrc.getBoundingBox(), omDest.getBoundingBox(), !suppressZ );
		
		double maxDistRslv = rslvDist(
			res,
			omSrc.getBoundingBox().midpoint(),
			omDest.getBoundingBox().midpoint()
		);
		
		if (dist>=maxDistRslv) {
			return false;
		} else {
		
			logReporter.logFormatted(
				"Maybe merging %s and %s with dist %f (<%f)",
				omSrc.getBoundingBox().midpoint(),
				omDest.getBoundingBox().midpoint(),
				dist,
				maxDistRslv
			);
			
			return true;
		}
	}
	
	private double rslvDist( Optional<ImageResolution> res, Point3d pnt1, Point3d pnt2 ) throws OperationFailedException {
		if (suppressZ) {
			return maxDist.rslv(res, new Point3d(pnt1.getX(),pnt1.getY(),0), new Point3d(pnt2.getX(),pnt2.getY(),0) );
		} else {
			return maxDist.rslv(res, pnt1, pnt2 );
		}
	}
}
