/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.stack.StackProviderHolder;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.object.ObjectCollection;
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

        SegmentText provider = createAndInitProvider("car.jpg");

        ObjectCollection objects = provider.create();

        assertTrue(objects.size() == 3);

        assertBoxAtIndex(objects, 0, boxAt(439, 311, 75, 37));
        assertBoxAtIndex(objects, 1, boxAt(310, 318, 108, 36));
        assertBoxAtIndex(objects, 2, boxAt(392, 199, 29, 26));
    }

    private void assertBoxAtIndex(ObjectCollection objects, int index, BoundingBox box) {
        assertEquals("box at index " + index, box, objects.get(index).getBoundingBox());
    }

    private SegmentText createAndInitProvider(String imageFilename) throws InitException {

        SegmentText provider = new SegmentText();

        Stack stack = testLoader.openStackFromTestPath(imageFilename);
        provider.setStackProvider(new StackProviderHolder(stack));

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
        return new BoundingBox(new Point3i(x, y, 0), new Extent(width, height, 1));
    }
}
