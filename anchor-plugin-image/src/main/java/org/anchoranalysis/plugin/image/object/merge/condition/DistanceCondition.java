package org.anchoranalysis.plugin.image.object.merge.condition;

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
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.BoundingBoxDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DistanceCondition implements BeforeCondition {

	private final UnitValueDistance maxDist;
	private final boolean suppressZ;
	private final MessageLogger logger;
	
	@Override
	public boolean accept(ObjectMask source, ObjectMask destination, Optional<ImageResolution> res) throws OperationFailedException {
		
		// We impose a max dist condition if necessary
		if (maxDist!=null) {
			return isWithinMaxDist(source,destination,res);
		} else {
			return true;
		}
	}
	
	private boolean isWithinMaxDist( ObjectMask source, ObjectMask destination, Optional<ImageResolution> res ) throws OperationFailedException {
		
		double dist = BoundingBoxDistance.distance( source.getBoundingBox(), destination.getBoundingBox(), !suppressZ );
		
		double maxDistRslv = rslvDist(
			res,
			source.getBoundingBox().midpoint(),
			destination.getBoundingBox().midpoint()
		);
		
		if (dist>=maxDistRslv) {
			return false;
		} else {
		
			logger.logFormatted(
				"Maybe merging %s and %s with dist %f (<%f)",
				source.getBoundingBox().midpoint(),
				destination.getBoundingBox().midpoint(),
				dist,
				maxDistRslv
			);
			
			return true;
		}
	}
	
	private double rslvDist( Optional<ImageResolution> res, Point3d point1, Point3d point2 ) throws OperationFailedException {
		if (suppressZ) {
			return maxDist.rslv(res, new Point3d(point1.getX(),point1.getY(),0), new Point3d(point2.getX(),point2.getY(),0) );
		} else {
			return maxDist.rslv(res, point1, point2 );
		}
	}
}
