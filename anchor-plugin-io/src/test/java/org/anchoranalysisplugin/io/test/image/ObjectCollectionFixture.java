/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysisplugin.io.test.image;

import java.nio.ByteBuffer;
import java.util.Random;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

class ObjectCollectionFixture {

    // Maximum extent of a scene
    private static final Extent SCENE_EXTENT = new Extent(1000, 900, 40);

    /** How frequently do we change value randomly when iterating over pixel values */
    private static final double PROBABILITY_CHANGE_VALUE = 0.2;

    private static final Random RANDOM = new Random();

    /**
     * Creates an object-collection containing between a random number of objects (uniformly sampled
     * from a range)
     *
     * <p>Each object has a position and extent that is randomly-sampled, and contains voxels that are
     * each randomly on or off.
     *
     * @param minNumberObjects minimum number of objects (inclusive)
     * @param maxNumberObjects maximum number of objects (exclusive)
     * @return
     */
    public ObjectCollection createMockObjects(int minNumberObjects, int maxNumberObjects) {
        int numberObjects = randomMinMax(minNumberObjects, maxNumberObjects);
        return ObjectCollectionFactory.fromRepeated(numberObjects, this::mockObject);
    }

    private ObjectMask mockObject() {
        Extent e = randomExtent();
        Point3i crnr = randomCorner(e);
        return mockObject(crnr, e);
    }

    private ObjectMask mockObject(Point3i corner, Extent e) {

        ObjectMask object = new ObjectMask(new BoundingBox(corner, e));

        int volumeXY = e.volumeXY();
        for (int z = 0; z < e.z(); z++) {

            ByteBuffer bb = object.sliceBufferLocal(z);

            int prevVal = 0;

            for (int i = 0; i < volumeXY; i++) {
                prevVal = randomMaybeChangeValue(prevVal);
                bb.put((byte) prevVal);
            }
        }

        // Switch samples on an off with uniform randomness
        return object;
    }

    /** Randomly returns 0 or 255 with equal probability. prevVal must be 0 or 255 */
    private static int randomMaybeChangeValue(int previousValue) {
        if (RANDOM.nextDouble() > PROBABILITY_CHANGE_VALUE) {
            return previousValue;
        } else {
            return (255 - previousValue);
        }
    }

    /** Randomly returns an extent within a scene */
    private static Extent randomExtent() {
        int x = randomTotal(SCENE_EXTENT.x() - 1) + 1;
        int y = randomTotal(SCENE_EXTENT.y() - 1) + 1;
        int z = randomTotal(SCENE_EXTENT.z() - 1) + 1;
        return new Extent(x, y, z);
    }

    /** A random starting corner, making sure there's enough room for the extent */
    private static Point3i randomCorner(Extent extent) {
        int x = randomSubtract(SCENE_EXTENT.x(), extent.x());
        int y = randomSubtract(SCENE_EXTENT.y(), extent.y());
        int z = randomSubtract(SCENE_EXTENT.z(), extent.z());
        return new Point3i(x, y, z);
    }

    private static int randomTotal(int total) {
        return RANDOM.nextInt(total);
    }

    private static int randomMinMax(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    private static int randomSubtract(int total, int sub) {
        return randomTotal(total - sub);
    }
}
