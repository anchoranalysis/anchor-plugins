/* (C)2020 */
package org.anchoranalysis.plugin.ij.bean.object.provider;

import static org.junit.Assert.*;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.junit.Test;

public class WalkShortestPathTest {

    @Test
    public void test() throws OperationFailedException {

        Point3i point1 = new Point3i(4, 19, 0);
        Point3i point2 = new Point3i(11, 3, 0);

        ObjectMask object = WalkShortestPath.walkLine(point1, point2);

        assertTrue(object.binaryVoxelBox().countOn() == 24);
        assertTrue(object.contains(new Point3i(4, 19, 0)));
        assertTrue(object.contains(new Point3i(11, 3, 0)));
    }
}
