package org.anchoranalysis.plugin.opencv.nonmaxima;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.combine.ObjectMaskMerger;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ClipOverlappingObjects;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.test.image.object.CircleObjectFixture;
import org.junit.Test;

public class ClipOverlappingObjectsTest {
    
    private static final int NUMBER_CIRCLES = 7;
    
    @Test
    public void testClipping() {
        
        SegmentedObjects segments = createOverlappingCircles();
        
        ClipOverlappingObjects clipper = new ClipOverlappingObjects();
        SegmentedObjects clipped = new SegmentedObjects( clipper.reduce(segments.asList()) );
        
        assertEquals("identical number of voxels", countTotalVoxels(segments), countTotalVoxels(clipped) );
        assertEquals("highest confidence object unchanged", segments.highestConfidence(), clipped.highestConfidence() );
    }
    
    private static SegmentedObjects createOverlappingCircles() {
        ObjectCollection circles = CircleObjectFixture.successiveCircles(NUMBER_CIRCLES, new Point2d(15,15), 5, new Point2d(8,8), 1);
        
        // Add confidence successively from 0.2 (inclusive) to 0.8 (inclusive) in 0.1 increments 
        List<WithConfidence<ObjectMask>> list = FunctionalList.mapToListWithIndex(circles.asList(), (object, index) -> new WithConfidence<>(object, (index*0.1)+0.2 ) );
        
        return new SegmentedObjects(list);
    }
    
    private static int countTotalVoxels(SegmentedObjects segments) {
        try {
            return ObjectMaskMerger.merge(segments.asObjects()).numberVoxelsOn();
        } catch (OperationFailedException e) {
            throw new AnchorFriendlyRuntimeException(e);
        }
    }
}
