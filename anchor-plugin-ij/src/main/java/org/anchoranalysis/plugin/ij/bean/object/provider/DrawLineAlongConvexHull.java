package org.anchoranalysis.plugin.ij.bean.object.provider;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;

/**
 * Draws a lines between successive points on the convex-hull of an object. 
 * <p>
 * For each object:
 *   1. extracts the convex hull of the outline (a set of points)
 *   2. connects these points together by walking a line between them. This ensures it is a single connected component.
 *    
 * This only currently works in 2D on coplanar (XY) points.
 * 
 * @author feehano
 *
 */
public class DrawLineAlongConvexHull extends ObjectCollectionProviderWithDimensions {
	
	@Override
	public ObjectCollection createFromObjects( ObjectCollection objects ) throws CreateException {
		ImageDimensions dim = createDimensions();
		return objects.stream().map( object ->
			transform(object,dim)
		);
	}
	
	private ObjectMask transform(ObjectMask object, ImageDimensions dimensions) throws CreateException {
		try {
			List<Point2i> pointsConvexHull = ConvexHullUtilities.convexHull2D(
				ConvexHullUtilities.pointsOnOutline(object)
			);
			
			if (pointsConvexHull.size()<=1) {
				return object;
			}

			return WalkShortestPath.walkLine(
				PointConverter.convert2iTo3d(pointsConvexHull)
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
}