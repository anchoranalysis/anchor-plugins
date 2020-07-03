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

import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;

import ch.ethz.biol.cell.mpp.mark.MarkPolygon;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ObjMaskPolygonCreator {

	/** Given a list of points that define a polygon, extract a corresponding object-mask */
	public static ObjectMask create( List<Point3d> pnts3d, ImageDimensions sd ) {

		// Add each point to a MarkPolygon
		MarkPolygon polygon = polygonForPoints(pnts3d);
		
		return polygon.calcMask(
			sd,
			RegionMapSingleton.instance().membershipWithFlagsForIndex(0),
			BinaryValuesByte.getDefault()
		).getMask();		
	}
	
	private static MarkPolygon polygonForPoints( List<Point3d> pnts3i ) {
		MarkPolygon polygon = new MarkPolygon();
		polygon.getPoints().addAll(pnts3i);
		polygon.updateAfterPointsChange();
		return polygon;
	}
}
