package org.anchoranalysis.plugin.opencv.nonmaxima;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ConditionallyMergeOverlappingObjects;
import org.junit.Test;

public class ConditionallyMergeOverlappingObjectsTest {
    
    private ReduceElementsTester tester = new ReduceElementsTester();
    
    @Test
    public void testReduce() throws OperationFailedException {
        tester.test(new ConditionallyMergeOverlappingObjects(), true, 5, 0.8);
    }
}
