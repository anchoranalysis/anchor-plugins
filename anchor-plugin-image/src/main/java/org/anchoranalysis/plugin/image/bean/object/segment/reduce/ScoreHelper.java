package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ScoreHelper {
    
    public static double confidenceDifference(WithConfidence<ObjectMask> source, WithConfidence<ObjectMask> overlapping) {
        return source.getConfidence() - overlapping.getConfidence();
    }
    
    public static double overlapScore(int numberVoxelsClipped, ObjectMask sourceElement) {
        // The latter part of this calculation is likely being repeated. Consider caching.
        return ((double) numberVoxelsClipped) / sourceElement.numberVoxelsOn(); 
    }
}
