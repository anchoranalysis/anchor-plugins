package org.anchoranalysis.plugin.points.bean.object.provider;

/*-
 * #%L
 * anchor-plugin-points
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromPointsFactory;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline.ContourList;
import ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline.SplitContourSmoothingSpline;
import lombok.Getter;
import lombok.Setter;

/**
 * Splits a 2D contour represented by an object-mask into several contours, splitting at "turn" points.
 * <p>
 * Specifically, smoothing spline interpolation is performed along the contour and splits occur at saddle points.
 * <p> 
 * Each contour is represented by an input object.
 *  
 * @author Owen Feehan
 *
 */
public class SplitContourAtSaddlePoints extends ObjectCollectionProviderUnary {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private double smoothingFactor = 0.001;
	
	@BeanField @Getter @Setter
	private int numberLoopPoints = 0;
	
	/** If a contour has less than this number of points, we don't split it, and return it as-is */
	@BeanField @Getter @Setter
	private int minNumberPoints = 10;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjects( ObjectCollection objects ) throws CreateException {
		return objects.stream().flatMapWithException(
			CreateException.class,
			this::splitContoursFromObject
		);
	}
	
	private ObjectCollection splitContoursFromObject(ObjectMask object) throws CreateException {
		
		if (object.getBoundingBox().extent().getZ()>1) {
			throw new CreateException("Only objects with z-slices > 1 are allowed");
		}
		
		try {
			return contoursAsObjects(
				SplitContourSmoothingSpline.apply(
					object,
					smoothingFactor,
					numberLoopPoints,
					minNumberPoints
				)
			);
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}		
	}
	
	private static ObjectCollection contoursAsObjects( ContourList contourList ) throws OperationFailedException {
		try {
			return ObjectCollectionFactory.mapFrom(
				contourList,
				contour -> createObjectFromContour(contour, true)
			);
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static ObjectMask createObjectFromContour( Contour contour, boolean round ) throws CreateException {
		return CreateFromPointsFactory.create(
			PointConverter.convert3i(contour.getPoints(), round)
		);
	}
}
