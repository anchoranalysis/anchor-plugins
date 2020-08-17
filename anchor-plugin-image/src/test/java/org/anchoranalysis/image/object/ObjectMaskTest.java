package org.anchoranalysis.image.object;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2d;
import org.junit.Test;
import static org.anchoranalysis.image.object.CheckVolumeBeforeAfter.*;
import static org.junit.Assert.assertTrue;
import java.util.Optional;

public class ObjectMaskTest {
    
    /** Expected number of voxels in the intersection of the two circular objects */
    private static final int EXPECTED_NUMBER_VOXELS_INTERSECTION = 241;
    
    /**
     * How much to scale the objects by (in each of the X and Y dimensions)
     */
    private static final int SCALE_FACTOR = 7;
    
    /** Scales up an object-mask with an interpolator 
     * @throws OperationFailedException */
    @Test
    public void testScaleUp() throws OperationFailedException {
        
        ScaledObjectAreaChecker checker = new ScaledObjectAreaChecker(SCALE_FACTOR);
        
        // Create an object that is a small circle
        ObjectMask unscaled = CircleObjectFixture.circleAt( new Point2d(8,8), 7);
        checker.assertConnected("unscaled", unscaled);
        
        ObjectMask scaled = unscaled.scale(checker.factor());
        
        checker.assertConnected("scaled", scaled);
        checker.assertExpectedArea(unscaled, scaled);
    }
    
    @Test
    public void testIntersect() throws OperationFailedException {
        
        ObjectCollection objects = TwoIntersectingCirclesFixture.create();

        // Check the circular objects have the volume that is expected
        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objects,"");
        
        Optional<ObjectMask> intersection = objects.get(0).intersect( objects.get(1), TwoIntersectingCirclesFixture.DIMENSIONS);
        
        // Check the circular objects have the same volume as before
        TwoIntersectingCirclesFixture.checkVolumesOnCircles(objects,"");
        
        assertTrue("intersection is defined", intersection.isPresent());
        assertDiscreteVolume("intersection", EXPECTED_NUMBER_VOXELS_INTERSECTION, intersection.get());
    }
}
