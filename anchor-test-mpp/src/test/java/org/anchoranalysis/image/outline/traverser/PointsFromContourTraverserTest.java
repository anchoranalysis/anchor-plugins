/*-
 * #%L
 * anchor-test-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.image.outline.traverser;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.traverser.path.PointsListNeighborUtilities;
import org.anchoranalysis.plugin.opencv.CVFindContours;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.anchoranalysis.test.mpp.LoadUtilities;
import org.junit.Test;

public class PointsFromContourTraverserTest {

    private TestLoaderImageIO testLoader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    @Test
    public void test01()
            throws CreateException, OperationFailedException, SetOperationFailedException {
        testFor("01");
    }

    private void testFor(String suffix) throws CreateException, OperationFailedException {

        ObjectMask objIn = LoadUtilities.openLargestObjectFrom(suffix, testLoader);

        // Checks that first and last points are neighbors
        List<Contour> contours = CVFindContours.contoursForObject(objIn);
        for (Contour contour : contours) {
            List<Point3i> points = contour.pointsDiscrete();
            assertTrue(doesFirstNeighborLast(points));
            assertTrue(PointsListNeighborUtilities.areNeighborsDistinct(points));
            assertTrue(PointsListNeighborUtilities.areAllPointsInBigNeighborhood(points));
        }
    }

    private static boolean doesFirstNeighborLast(List<Point3i> points) {
        Point3i first = points.get(0);
        Point3i last = points.get(points.size() - 1);
        return PointsListNeighborUtilities.arePointsNeighbors(first, last);
    }
}
