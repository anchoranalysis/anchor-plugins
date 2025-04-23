/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.image.task.feature.fixture;

import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;

/** Fixture for creating {@link MultiInput} objects for testing purposes. */
public class MultiInputFixture {

    /** The name used for the object collection in the {@link MultiInput}. */
    public static final String OBJECTS_NAME = "objectsTest";

    /** The number of intersecting objects to generate. */
    public static final int NUMBER_INTERSECTING_OBJECTS = 4;

    /** The number of non-intersecting objects to generate. */
    public static final int NUMBER_NOT_INTERSECTING_OBJECTS = 2;

    /**
     * A number of unique pairs of intersecting objects (known from the output of {@link
     * IntersectingCircleObjectsFixture#generateIntersectingObjects} with the parameterization
     * above.
     */
    public static final int NUMBER_PAIRS_INTERSECTING = 3;

    /**
     * Creates a {@link MultiInput} with an object-collection {@code OBJECTS_NAME}.
     *
     * <p>It contains 6 unique objects, 4 of whom intersect, and 2 who don't intersect at all.
     *
     * <p>Among the four who intersect, there are 3 intersections.
     *
     * <p>See the constants in the fixture to represent these numbers.
     *
     * @param energyStack the {@link EnergyStackWithoutParameters} to use in the {@link MultiInput}
     * @return a new {@link MultiInput} instance
     */
    public static MultiInput createInput(EnergyStackWithoutParameters energyStack) {
        MultiInput input =
                new MultiInput(
                        "input", new StackAsProviderFixture(energyStack.asStack(), "someName"));
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
