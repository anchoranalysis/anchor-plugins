package org.anchoranalysis.plugin.points.calculate.ellipse;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObjMask;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.ConicFitterBase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class EllipseFactory {

	private static final int MIN_NUMBER_POINTS = 10;
	
	private final ConicFitterBase pointsFitter;
	
	public MarkEllipse create(
		ObjectMask object,
		ImageDimensions dim,
		double shellRad
	) throws CreateException, InsufficientPointsException {

		pointsFitter.setShellRad(shellRad);
		
		Set<Point3i> points = PointsFromObjMask.pntsFromMaskOutlineSet(object);
		
		if (points.size()<MIN_NUMBER_POINTS) {
			throw new InsufficientPointsException(
				String.format("Only %d points. There must be at least %d points.", points.size(), MIN_NUMBER_POINTS)	
			);
		}

		return createEllipse(points, dim);
	}
	
	private MarkEllipse createEllipse(Set<Point3i> points, ImageDimensions dim) throws InsufficientPointsException, CreateException {
		
		MarkEllipse mark = new MarkEllipse();
		
		List<Point3f> ptsF = new ArrayList<>();
		for( Point3i p : points ) {
			ptsF.add( new Point3f(p.getX(),p.getY(),0) );
		}
		
		try {
			pointsFitter.fit( ptsF, mark, dim );
		} catch (PointsFitterException e) {
			throw new CreateException(e);
		}
		
		return mark;
	}
	
}
