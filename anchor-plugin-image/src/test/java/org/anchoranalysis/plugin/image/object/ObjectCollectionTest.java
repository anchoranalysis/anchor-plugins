/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.object;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.scale.ScaledElements;
import org.anchoranalysis.test.image.object.CircleObjectFixture;
import org.junit.Test;

/**
 * Tests methods on {@link ObjectCollection}.
 *
 * <p>It would better belong in {@code anchor-image} but is kept here instead to reuse a fixture.
 *
 * @author Owen Feehan
 */
public class ObjectCollectionTest {

    /** How much to scale the objects by (in each of the X and Y dimensions) */
    private static final int SCALE_FACTOR = 4;

    /** Tests scaling up on a small number of objects */
    @Test
    public void testScaleUpFew() throws OperationFailedException {
        testNumberCircles(5, 2);
    }

    /**
     * Tests scaling up on many objects (deliberately chosen to exceed the 255 objects possible with
     * an unsigned byte type)
     */
    @Test
    public void testScaleUpMany() throws OperationFailedException {
        testNumberCircles(300, 0.02);
    }

    /**
     * Scales up an object-collection with an interpolator and checks that the total area of the
     * objects is as expected after scaling
     *
     * @param numberCircles how many circles to use in the test
     * @param radiusIncrease how much to increase the radius by for successive circles
     */
    private void testNumberCircles(int numberCircles, double radiusIncrease)
            throws OperationFailedException {

        ScaledObjectAreaChecker checker = new ScaledObjectAreaChecker(SCALE_FACTOR);

        // Create several small circles, some intersecting and some not intersecting
        ObjectCollection unscaled =
                CircleObjectFixture.successiveCircles(
                        numberCircles, new Point2d(10, 10), 3, new Point2d(10, 8), radiusIncrease);

        ScaledElements<ObjectMask> scaled = unscaled.scale(checker.factor());

        checker.assertExpectedArea(unscaled, scaled);
        assertEquals(unscaled.size(), scaled.size());
    }
}
