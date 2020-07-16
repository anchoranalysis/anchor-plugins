/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;

class MultiInputFixture {

    public static final String OBJECTS_NAME = "objectsTest";

    public static final int NUMBER_INTERSECTING_OBJECTS = 4;
    public static final int NUMBER_NOT_INTERSECTING_OBJECTS = 2;

    /**
     * A number of unique pairs of intersecting objects (known from the output of {@link
     * IntersectingCircleObjectsFixture.generateIntersectingObjects} with the parameterization
     * above.
     */
    public static final int NUMBER_PAIRS_INTERSECTING = 3;

    /**
     * This creates a MultiInput with an object-collection {@code OBJECTS_NAME}
     *
     * <p>It contains 6 unique objects, 4 of whom intersect, and 2 who don't intersect at all.
     *
     * <p>Among the four who intersect, there are 3 intersections.
     *
     * <p>See the constants in the fixture to represent these numbers.
     *
     * @param nrgStack
     * @return
     */
    public static MultiInput createInput(NRGStack nrgStack) {
        MultiInput input =
                new MultiInput("input", new StackAsProviderFixture(nrgStack.asStack(), "someName"));
        input.objects()
                .add(
                        OBJECTS_NAME,
                        () ->
                                IntersectingCircleObjectsFixture.generateIntersectingObjects(
                                        NUMBER_INTERSECTING_OBJECTS,
                                        NUMBER_NOT_INTERSECTING_OBJECTS,
                                        false));
        return input;
    }
}
