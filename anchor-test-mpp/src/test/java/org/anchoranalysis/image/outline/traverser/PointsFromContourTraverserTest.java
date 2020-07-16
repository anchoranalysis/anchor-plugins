/* (C)2020 */
package org.anchoranalysis.image.outline.traverser;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.traverser.contiguouspath.PointsListNeighborUtilities;
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

        ObjectMask objIn = LoadUtilities.openLargestObjectBinaryFrom(suffix, testLoader);

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
