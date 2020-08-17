package org.anchoranalysis.image.object;

import static org.anchoranalysis.image.object.CheckVolumeBeforeAfter.assertCircularArea;
import static org.anchoranalysis.image.object.CheckVolumeBeforeAfter.assertDiscreteVolume;
import static org.junit.Assert.assertEquals;

import lombok.NoArgsConstructor;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.image.extent.ImageDimensions;

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
    public static final ImageDimensions DIMENSIONS = new ImageDimensions(60, 50, 1);

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
     * (after being ,odified)
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
