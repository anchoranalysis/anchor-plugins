package org.anchoranalysis.plugin.opencv.nonmaxima;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.image.bean.object.segment.reduce.ThresholdConfidence;
import org.junit.Test;

public class ThresholdConfidenceTest {

    private ReduceElementsTester tester = new ReduceElementsTester();

    @Test
    public void testReduce() throws OperationFailedException {
        tester.test(new ThresholdConfidence(0), false, 1, 0.56827);
    }
}
