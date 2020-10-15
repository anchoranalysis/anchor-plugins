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

import static org.anchoranalysis.plugin.image.object.CheckVolumeBeforeAfter.assertCircularArea;
import static org.anchoranalysis.plugin.image.object.CheckVolumeBeforeAfter.assertDiscreteVolume;
import static org.junit.Assert.assertEquals;

import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.test.image.object.CircleObjectFixture;

/**
 * Creates two intersecting circles
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class TwoIntersectingCirclesFixture {

    /** Radius size of first circle */
    public static final int RADIUS_FIRST = 11;

    /** Added to {@code RADIUS_FIRST} to form radius of second circle */
    private static final int RADIUS_INCREMENT = 4;

    /** Radius size of second circle */
    public static final int RADIUS_SECOND = RADIUS_FIRST + RADIUS_INCREMENT;

    /** Center point of first circle */
    private static final Point2d CENTER_POINT_FIRST = new Point2d(20, 20);

    /** Added to {@code CENTER_POINT_FIRST} to form center-point of second circle */
    private static final Point2d CENTER_POINT_INCREMENT = new Point2d(10, 5);

    /** Dimensions that span all the objects created */
    public static final Dimensions DIMENSIONS = new Dimensions(60, 50, 1);

    /** Friendly name to identify <i>first</i> object in messages */
    private static final String IDENTIFIER_FIRST = "first";

    /** Friendly name to identify <i>second</i> object in messages */
    private static final String IDENTIFIER_SECOND = "second";

    /**
     * Creates two intersecting circles with constant properties
     *
     * @return the newly created collection
     */
    public static ObjectCollection create() {
        return CircleObjectFixture.successiveCircles(
                2, CENTER_POINT_FIRST, RADIUS_FIRST, CENTER_POINT_INCREMENT, RADIUS_INCREMENT);
    }

    /**
     * Checks that two objects created by this fixture (first and second) have the volumes that are
     * as expected
     *
     * @param objects the two objects, respectively termed first and second
     * @param suffix added to the identifiers of each object for descriptive purposes
     */
    public static void checkVolumesOnCircles(ObjectCollection objects, String suffix) {
        assertEquals("objects are correct size", 2, objects.size());
        assertCircularArea(
                IDENTIFIER_FIRST + suffix,
                TwoIntersectingCirclesFixture.RADIUS_FIRST,
                objects.get(0));
        assertCircularArea(
                IDENTIFIER_SECOND + suffix,
                TwoIntersectingCirclesFixture.RADIUS_SECOND,
                objects.get(1));
    }

    /**
     * Checks that two objects created by this fixture (first and second) have specific volumes
     * (after being modified)
     *
     * @param objects the two objects, respectively termed first and second
     * @param suffix added to the identifiers of each object for descriptive purposes
     * @param expectedVolumeFirst the expected volume of the first object (after being modified)
     * @param expectedVolumeSecond the expected volume of the first object (after being modified)
     */
    public static void checkModifiedVolumesOnCircles(
            ObjectCollection objects,
            String suffix,
            int expectedVolumeFirst,
            int expectedVolumeSecond) {
        assertEquals("objects are correct size", 2, objects.size());
        assertDiscreteVolume(IDENTIFIER_FIRST + suffix, expectedVolumeFirst, objects.get(0));
        assertDiscreteVolume(IDENTIFIER_SECOND + suffix, expectedVolumeSecond, objects.get(1));
    }
}
