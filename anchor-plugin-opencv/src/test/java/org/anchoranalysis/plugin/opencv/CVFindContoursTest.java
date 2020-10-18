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

package org.anchoranalysis.plugin.opencv;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.core.points.PointsNeighborChecker;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.contour.Contour;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.object.TestLoaderObjects;
import org.junit.Test;

public class CVFindContoursTest {

    private TestLoaderObjects loader =
            new TestLoaderObjects(TestLoader.createFromMavenWorkingDirectory());

    @Test
    public void test01()
            throws CreateException, OperationFailedException, SetOperationFailedException {
        testFor("01");
    }

    private void testFor(String suffix) throws CreateException, OperationFailedException {

        ObjectMask object = loader.openLargestObjectFrom(suffix);

        // Checks that first and last points are neighbors
        for (Contour contour : CVFindContours.contoursForObject(object)) {
            List<Point3i> points = contour.pointsDiscrete();
            assertTrue(doesFirstNeighborLast(points));
            assertTrue(PointsNeighborChecker.areNeighborsDistinct(points));
            assertTrue(PointsNeighborChecker.areAllPointsInBigNeighborhood(points));
        }
    }

    private static boolean doesFirstNeighborLast(List<Point3i> points) {
        Point3i first = points.get(0);
        Point3i last = points.get(points.size() - 1);
        return PointsNeighborChecker.arePointsNeighbors(first, last);
    }
}
