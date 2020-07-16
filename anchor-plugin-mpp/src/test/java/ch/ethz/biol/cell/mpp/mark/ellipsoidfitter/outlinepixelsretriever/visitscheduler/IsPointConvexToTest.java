/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;
/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class IsPointConvexToTest {

    @Test
    public void test() throws RasterIOException, CreateException, URISyntaxException {

        TestLoaderImageIO testLoader =
                new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

        String testPath = "testData/binaryImage/000_zstack_nuclei.tif";
        Stack stack = testLoader.openStackFromTestPath(testPath);

        Mask bic = new Mask(stack.getChnl(0), BinaryValues.getDefault());

        BinaryVoxelBox<ByteBuffer> bvb = bic.binaryVoxelBox();

        Point3i pointRoot = new Point3i(62, 84, 15);

        Point3i pointCheck0 = new Point3i(57, 77, 15);
        assertTrue(VisitSchedulerConvexAboutRoot.isPointConvexTo(pointRoot, pointCheck0, bvb));

        Point3i pointCheck1 = new Point3i(69, 89, 17);
        assertTrue(VisitSchedulerConvexAboutRoot.isPointConvexTo(pointRoot, pointCheck1, bvb));

        Point3i pointCheck2 = new Point3i(81, 84, 16);
        assertFalse(VisitSchedulerConvexAboutRoot.isPointConvexTo(pointRoot, pointCheck2, bvb));
    }
}
