package org.anchoranalysis.plugin.opencv;

/*-
 * #%L
 * anchor-plugin-opencv
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.objmask.ObjMask;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/** Wrapper around Open CV's findContours function */
public class CVFindContours {

	static {
		CVInit.alwaysExecuteBeforeCallingLibrary();
	}
	
	public static List<Contour> contourForObjMask( ObjMask om, int minNumPoints ) throws OperationFailedException {
		
		try {
			// We clone ss the source image is modified by the algorithm according to OpenCV docs
			// https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
			Mat mat = MatConverter.fromObjMask(
				om.duplicate()
			);
						
			List<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );
			
			return convertMatOfPoint( contours, om.getBoundingBox().getCrnrMin() );
						
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static List<Contour> convertMatOfPoint( List<MatOfPoint> contours, Point3i crnrMin ) {
		return contours.stream().map(
			a -> CVFindContours.createContour(a, crnrMin )
		).collect( Collectors.toList() );
	}
	
	private static Contour createContour(MatOfPoint mop, Point3i crnrMin) {
		Contour c = new Contour();
		for( Point p : mop.toArray() ) {
			
			Point3f pnt = new Point3f(
				convertAdd(p.x, crnrMin.getX() ),
				convertAdd(p.y, crnrMin.getY() ),
				crnrMin.getZ()
			);
			
			c.getPoints().add(pnt);
		}
		return c;
	}
	
	private static float convertAdd( double in, double add ) {
		return (float) (in+add);
	}
	
}
