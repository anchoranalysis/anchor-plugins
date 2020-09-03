package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.opencv.nonmaxima.IntersectionOverUnion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ExpectedBoxesChecker {
    
    /**
     * Minimum intersection-over-union score required to be considered identical to a box.
     */
    private static final double THRESHOLD_SCORE = 0.9;
    
    public static void assertExpectedBoxes(ObjectCollection objects, List<BoundingBox> expectedBoxes) {
        expectedBoxes.stream().forEach( box->
            assertOverlap(objects, box)
        );        
    }

    private static void assertOverlap(ObjectCollection objects, BoundingBox box) {
        assertTrue(
                "at least one object has box: " + box.toString(),
                atLeastOneObjectOverlaps(objects, box));
    }

    private static boolean atLeastOneObjectOverlaps(ObjectCollection objects, BoundingBox box) {
        return objects.stream().anyMatch( object ->
            IntersectionOverUnion.forBoxes(object.boundingBox(), box) > THRESHOLD_SCORE
        );
    }
}
