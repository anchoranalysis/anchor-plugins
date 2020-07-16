/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.anchoranalysis.test.mpp.LoadUtilities;
import org.junit.Test;

public class SplitContourSmoothingSplineTest {

    private TestLoaderImageIO testLoader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    @Test
    public void test()
            throws CreateException, OperationFailedException, SetOperationFailedException {

        ObjectMask contourIn = LoadUtilities.openLargestObjectBinaryFrom("01", testLoader);

        ContourList contours = SplitContourSmoothingSpline.apply(contourIn, 0.001, 0, 30);

        assertEquals(72, contours.size());
    }
}
