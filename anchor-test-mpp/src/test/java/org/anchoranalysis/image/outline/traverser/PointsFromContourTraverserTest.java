package org.anchoranalysis.image.outline.traverser;

/*-
 * #%L
 * anchor-test-mpp
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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.outline.traverser.contiguouspath.PointsListNghbUtilities;
import org.anchoranalysis.plugin.opencv.CVFindContours;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.anchoranalysis.test.mpp.LoadUtilities;
import org.junit.Test;

public class PointsFromContourTraverserTest {
	
	private static int MIN_NUM_CONTOUR_PNTS = 30;
	
	private TestLoaderImageIO testLoader = new TestLoaderImageIO(
		TestLoader.createFromMavenWorkingDir()
	);
	
	@Test
	public void test01() throws CreateException, OperationFailedException, SetOperationFailedException {
		testFor("01");
	}
		
	private void testFor( String suffix ) throws CreateException, OperationFailedException {

		ObjectMask objIn = LoadUtilities.openLargestObjectBinaryFrom(suffix, testLoader);
		
		// Checks that first and last points are neighbours
		List<Contour> contours = CVFindContours.contourForObjMask(objIn, MIN_NUM_CONTOUR_PNTS);
		for( Contour c : contours ) {
			List<Point3i> pts = c.pointsDiscrete();
			assertTrue( areFirstLastNghb(pts) );
			assertTrue( PointsListNghbUtilities.areNghbDistinct(pts) );
			assertTrue( PointsListNghbUtilities.areAllPointsInBigNghb(pts) );
		}
	}
		
	private static boolean areFirstLastNghb( List<Point3i> pts ) {
		Point3i first = pts.get(0);
		Point3i last = pts.get( pts.size() -1 );
		return PointsListNghbUtilities.arePointsNghb(first, last );		
	}
}
