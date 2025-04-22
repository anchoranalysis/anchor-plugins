/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.core.log.CommonContext;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculator;
import org.anchoranalysis.test.feature.plugins.objects.CircleObjectFixture;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.mockito.Mockito;

/** Utility operations for unning tests that involve intersecting objects. */
class IntersectingObjectsTestHelper {

    /** Identifier for the shared object collection. */
    private static final String ID = "someObjects";

    /** Number of intersecting objects to generate. */
    private static final int NUMBER_INTERSECTING = 4;

    /** Number of non-intersecting objects to generate. */
    private static final int NUMBER_NOT_INTERSECTING = 2;

    /**
     * Runs several tests on a feature and object-mask collection by removing objects at particular
     * indexes.
     *
     * <p>Specifically the test that is called is the same as {@link #assertFeatureIndexInt}.
     *
     * @param messagePrefix prefix for the test message
     * @param feature feature to use for test
     * @param sameSize whether the objects should be the same size
     * @param expectedFirst expected result for object in first position
     * @param expectedSecond expected result for object in second position
     * @param expectedSecondLast expected result for object in second-last position
     * @param expectedLast expected result for object in last position
     * @throws OperationFailedException if the operation fails
     * @throws FeatureCalculationException if feature calculation fails
     * @throws InitializeException if initialization fails
     */
    public static void testPositions(
            String messagePrefix,
            FeatureIntersectingObjects feature,
            boolean sameSize,
            int expectedFirst,
            int expectedSecond,
            int expectedSecondLast,
            int expectedLast)
            throws OperationFailedException, FeatureCalculationException, InitializeException {

        ObjectCollection objects =
                IntersectingCircleObjectsFixture.generateIntersectingObjects(
                        NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, sameSize);

        // First object
        IntersectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "first"), feature, objects, 0, expectedFirst);

        // Second object
        IntersectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "second"), feature, objects, 1, expectedSecond);

        // Second last object
        int secondLastIndex = (NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING) - 2;
        IntersectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "second-last"),
                feature,
                objects,
                secondLastIndex,
                expectedSecondLast);

        // Last object
        int lastIndex = (NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING) - 1;
        IntersectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "last"), feature, objects, lastIndex, expectedLast);
    }

    /**
     * Asserts a result after extracting object at index {@code i} from a collection, and using the
     * remainder as the {@link ObjectCollection}.
     *
     * @param message descriptive message for test
     * @param feature feature to calculate on inputs to form value
     * @param objects object-collection used to determine parameter for feature (single object
     *     removed at index) and the remainder that form a set of objects to intersect with
     * @param index index of object in collection to remove and use as parameter
     * @param expectedResult expected result from test
     * @throws InitializeException if initialization fails
     * @throws FeatureCalculationException if feature calculation fails
     * @throws OperationFailedException if the operation fails
     */
    private static void assertFeatureIndexInt(
            String message,
            FeatureIntersectingObjects feature,
            ObjectCollection objects,
            int index,
            int expectedResult)
            throws OperationFailedException, FeatureCalculationException {

        // We take the second object in the collection, as one that should intersect with 2 others
        ObjectMask objectMask = objects.get(index);
        ObjectCollection others = removeImmutable(objects, index);

        // We take the final objection the collection , as one

        FeatureTestCalculator.assertIntResult(
                message,
                addId(feature),
                new FeatureInputSingleObject(objectMask, CircleObjectFixture.energyStack()),
                Optional.of(createInitialization(others).sharedObjects()),
                expectedResult);
    }

    /**
     * Removes an object from the collection immutably.
     *
     * @param objects the original object collection
     * @param index the index of the object to remove
     * @return a new {@link ObjectCollection} with the object at the specified index removed
     */
    private static ObjectCollection removeImmutable(ObjectCollection objects, int index) {
        ArrayList<ObjectMask> removed = new ArrayList<>(objects.size() - 1);
        for (int i = 0; i < index; i++) {
            removed.add(objects.get(i));
        }
        for (int i = index + 1; i < objects.size(); i++) {
            removed.add(objects.get(i));
        }
        return new ObjectCollection(removed);
    }

    /**
     * Creates an {@link ImageInitialization} with the given object collection.
     *
     * @param others the object collection to include in the initialization
     * @return a new {@link ImageInitialization} instance
     * @throws OperationFailedException if the operation fails
     */
    private static ImageInitialization createInitialization(ObjectCollection others)
            throws OperationFailedException {

        SharedObjects sharedObjects =
                new SharedObjects(
                        new CommonContext(
                                LoggerFixture.suppressedLogger(), Mockito.mock(Path.class)));

        sharedObjects.getOrCreate(ObjectCollection.class).add(ID, () -> others);

        return new ImageInitialization(sharedObjects);
    }

    /**
     * Adds an ID to the given feature.
     *
     * @param feature the feature to add the ID to
     * @return the feature with the ID added
     */
    private static FeatureIntersectingObjects addId(FeatureIntersectingObjects feature) {
        feature.setId(ID);
        return feature;
    }

    /**
     * Combines two strings with a hyphen.
     *
     * @param first the first string
     * @param second the second string
     * @return the combined string
     */
    private static String combine(String first, String second) {
        return first + "-" + second;
    }
}
