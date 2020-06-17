package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*-
 * #%L
 * anchor-plugin-ij
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

import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;

/**
 * For each object:
 *   1. extracts the convex hull of the outline (a set of points)
 *   2. connects these points together by walking a line between them. This ensures it is a single ocnnected component.
 *    
 * This only currently works in 2D on coplanar (XY) points.
 * 
 * @author feehano
 *
 */
public class ObjMaskProviderConvexHullConnectLines extends ObjMaskProviderDimensions {
	
	@Override
	public ObjectCollection createFromObjs( ObjectCollection objsCollection ) throws CreateException {

		ImageDim dim = createDim();
		return objsCollection.map( om->transform(om,dim) );
	}
	
	private ObjectMask transform( ObjectMask obj, ImageDim sd ) throws CreateException {
		
		Optional<List<Point2i>> pntsConvexHull = ConvexHullUtilities.extractPointsFromOutline(obj, 1, true);
		
		if (!pntsConvexHull.isPresent() || pntsConvexHull.get().size()<=1) {
			return obj;
		}
		
		List<Point3d> pnts3d = PointConverter.convert2i_3d(pntsConvexHull.get());

		try {
			return ObjMaskWalkShortestPath.walkLine( pnts3d );
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
}
