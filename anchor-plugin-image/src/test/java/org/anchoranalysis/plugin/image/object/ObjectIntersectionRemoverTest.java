package org.anchoranalysis.plugin.image.object;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.TwoIntersectingCirclesFixture;
import org.junit.Test;

public class ObjectIntersectionRemoverTest {
    
    /** Expected number of voxels for the <i>first</i> object after removal of intersection */
    private static final int EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_FIRST = 136;
    
    /** Expected number of voxels for the <i>second</i> object after removal of intersection */
    private static final int EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_SECOND = 468;
    
    /** Removes intersecting voxels from two circular-objects and checks volumes are as expected 
     * @throws OperationFailedException */
    @Test
    public void testRemoveIntersection() throws OperationFailedException {
        
        // Create several small circles, some intersecting and some not intersecting
        ObjectCollection objectsBefore = TwoIntersectingCirclesFixture.create();
        
        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objectsBefore, " before");

        // Remove the intersecting voxelts
        ObjectCollection objectsAfter = ObjectIntersectionRemover.removeIntersectingVoxels(objectsBefore, TwoIntersectingCirclesFixture.DIMENSIONS, false);
        
        TwoIntersectingCirclesFixture.checkModifiedVolumesOnCircles(objectsAfter, "after", EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_FIRST, EXPECTED_NUMBER_VOXELS_AFTER_REMOVAL_SECOND);
    }
}
