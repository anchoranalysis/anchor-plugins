package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.spatial.box.BoundingBoxFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RemoveOverlappingObjects}.
 *
 * @author Owen Feehan
 */
class RemoveOverlappingObjectsTest {

    @Test
    void testWithoutInvert() {
        doTest(false);
    }

    @Test
    void testWithInvert() {
        doTest(true);
    }

    private void doTest(boolean invert) {
        RemoveOverlappingObjects remove = new RemoveOverlappingObjects();
        List<WithConfidence<ObjectMask>> objectsReduced = remove.reduce(allObjects(invert));
        assertEquals(3, objectsReduced.size());
    }

    /**
     * All the objects that are reduced during the test.
     *
     * @param invert if true, the object's ON pixels are 0. if false, they are 255.
     * @return a list of 4 objects to be used in the test.
     */
    private static List<WithConfidence<ObjectMask>> allObjects(boolean invert) {
        WithConfidence<ObjectMask> OBJECT1 = object(10, 10, 0.9, invert);
        WithConfidence<ObjectMask> OBJECT2 = object(11, 10, 0.8, invert);
        WithConfidence<ObjectMask> OBJECT3 = object(15, 10, 0.75, invert);
        WithConfidence<ObjectMask> OBJECT4 = object(40, 10, 0.7, invert);

        return Arrays.asList(OBJECT1, OBJECT2, OBJECT3, OBJECT4);
    }

    /**
     * Creates an {@link ObjectMask} in three-dimensions, corresponding to a box.
     *
     * @param coordinate the minimum point in the object-mask in all dimensions.
     * @param extent the size of the box that forms the object-mask in all dimensions.
     * @param confidence the confidence to associate with the object-mask
     * @param invert if true, the object's ON pixels are 0. if false, they are 255.
     * @return a newly created {@link ObjectMask} with associated confidence.
     */
    private static WithConfidence<ObjectMask> object(
            int coordinate, int extent, double confidence, boolean invert) {
        ObjectMask object = new ObjectMask(BoundingBoxFactory.uniform3D(coordinate, extent));
        if (invert) {
            object = object.invert();
        } else {
            object.assignOn().toAll();
        }
        return new WithConfidence<ObjectMask>(object, confidence);
    }
}
