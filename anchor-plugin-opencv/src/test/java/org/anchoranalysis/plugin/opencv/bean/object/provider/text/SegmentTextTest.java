/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import static org.junit.Assert.assertTrue;
import org.anchoranalysis.bean.ProviderHolder;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.BoundIOContextFixture;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

/**
 * Tests {@link SegmentText}
 *
 * <p>Note that the weights file for the EAST model is duplicated in src/test/resources, as well as
 * its usual location in the models/ directory of the Anchor distribution. This is as it's difficult
 * to determine a path to the models/ directory at test-time
 *
 * @author Owen Feehan
 */
public class SegmentTextTest {

    private TestLoaderImageIO testLoader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    @Test
    public void testCar() throws AnchorIOException, CreateException, InitException {

        Stack stack = createStack("car.jpg");

        SegmentText provider = createAndInitProvider(stack);

        ObjectCollection objects = provider.create();

        assertTrue(objects.size() == 3);

        assertAtLeastOneObjectHasBox(objects, boxAt(312, 319, 107, 34));
        assertAtLeastOneObjectHasBox(objects, boxAt(394, 200, 27, 25));
        assertAtLeastOneObjectHasBox(objects, boxAt(440, 312, 73, 36));
    }

    private Stack createStack(String imageFilename) {
        return testLoader.openStackFromTestPath(imageFilename);
    }

    private void assertAtLeastOneObjectHasBox(ObjectCollection objects, BoundingBox box) {
        assertTrue(
                "at least one object has box: " + box.toString(),
                atLeastOneObjectHasBox(objects, box));
    }

    private boolean atLeastOneObjectHasBox(ObjectCollection objects, BoundingBox box) {
        for (ObjectMask object : objects) {
            if (object.boundingBox().equals(box)) {
                return true;
            }
        }
        return false;
    }

    private SegmentText createAndInitProvider(Stack stack) throws InitException {

        SegmentText provider = new SegmentText();

        provider.setStack(new ProviderHolder<>(stack));

        initProvider(
                provider,
                BoundIOContextFixture.withSuppressedLogger(testLoader.getTestLoader().getRoot()));

        return provider;
    }

    private static void initProvider(SegmentText provider, BoundIOContext context)
            throws InitException {
        provider.init(ImageInitParamsFactory.create(context), context.getLogger());
    }

    /** Bounding box at particular point and coordinates */
    private static BoundingBox boxAt(int x, int y, int width, int height) {
        return new BoundingBox(new Point3i(x, y, 0), new Extent(width, height));
    }
}
