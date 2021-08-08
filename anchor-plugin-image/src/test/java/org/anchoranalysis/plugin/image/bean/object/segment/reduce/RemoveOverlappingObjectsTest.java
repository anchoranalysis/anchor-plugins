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
 *
 */
class RemoveOverlappingObjectsTest {

    // START FIRST CLUSTER
    public static final WithConfidence<ObjectMask> OBJECT1 = object(10, 10, 0.9);
    public static final WithConfidence<ObjectMask> OBJECT2 = object(11, 10, 0.8);
    public static final WithConfidence<ObjectMask> OBJECT3 = object(15, 10, 0.75);
    // END FIRST CLUSTER

    // START SECOND CLUSTER
    public static final WithConfidence<ObjectMask> OBJECT4 = object(40, 10, 0.7);
    // END SECOND CLUSTER
    
    @Test
    void testReduce() {
        List<WithConfidence<ObjectMask>> objects = Arrays.asList(OBJECT1, OBJECT2, OBJECT3, OBJECT4);
        
        RemoveOverlappingObjects remove = new RemoveOverlappingObjects();
        List<WithConfidence<ObjectMask>> objectsReduced = remove.reduce(objects);
        assertEquals(3, objectsReduced.size());
    }
    
    private static WithConfidence<ObjectMask> object(int coordinate, int extent, double confidence) {
        ObjectMask object = new ObjectMask( BoundingBoxFactory.uniform3D(coordinate, extent) );
        object = object.invert();
        return new WithConfidence<ObjectMask>(object, confidence);
    }
}
