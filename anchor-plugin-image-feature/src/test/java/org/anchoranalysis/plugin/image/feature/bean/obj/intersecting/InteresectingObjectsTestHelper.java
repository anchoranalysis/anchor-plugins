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
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculator;
import org.anchoranalysis.test.feature.plugins.objects.CircleObjectFixture;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;
import org.mockito.Mockito;

class InteresectingObjectsTestHelper {

    private static final String ID = "someObjects";

    private static final int NUMBER_INTERSECTING = 4;
    private static final int NUMBER_NOT_INTERSECTING = 2;

    /**
     * Runs several tests on a feature and object-mask collection by removing objects at particular
     * indexes
     *
     * <p>Specifically the test that is called is the same as {#link assertFeatureIndexInt}
     *
     * @param feature feature to use for test
     * @param expectedFirst expected-result for object in first-position
     * @param expectedSecond expected-result for object in second-position
     * @param expectedSecondLast expected-result for object in second-last position
     * @param expectedLast expected-result for object in last position
     * @throws OperationFailedException
     * @throws FeatureCalculationException
     * @throws InitializeException
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
        InteresectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "first"), feature, objects, 0, expectedFirst);

        // Second object
        InteresectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "second"), feature, objects, 1, expectedSecond);

        // Second last object
        int secondLastIndex = (NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING) - 2;
        InteresectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "second-last"),
                feature,
                objects,
                secondLastIndex,
                expectedSecondLast);

        // Last object
        int lastIndex = (NUMBER_INTERSECTING + NUMBER_NOT_INTERSECTING) - 1;
        InteresectingObjectsTestHelper.assertFeatureIndexInt(
                combine(messagePrefix, "last"), feature, objects, lastIndex, expectedLast);
    }

    /**
     * Asserts a result after extracting object at index i from a collection, and using the
     * remainder as the object-collection
     *
     * @param message descriptive-message for test
     * @param feature feature to calculate on params to form value
     * @param objects object-collection used to determine parameter for feature (single object
     *     removed at index) and the remainder that form a set of objects to intersect with
     * @param index index of object in collection to remove and use as parameter
     * @param expectedResult expected result from test
     * @throws InitializeException
     * @throws FeatureCalculationException
     * @throws OperationFailedException
     */
    private static void assertFeatureIndexInt(
            String message,
            FeatureIntersectingObjects feature,
            ObjectCollection objects,
            int index,
            int expectedResult)
            throws OperationFailedException, FeatureCalculationException, InitializeException {

        // We take the second object in the collection, as one that should intersect with 2 others
        ObjectMask objectMask = objects.get(index);
        ObjectCollection others = removeImmutable(objects, index);

        // We take the final objection the collection , as one

        FeatureTestCalculator.assertIntResult(
                message,
                addId(feature),
                new FeatureInputSingleObject(objectMask, CircleObjectFixture.energyStack()),
                Optional.of(createInitialization(others).getSharedObjects()),
                expectedResult);
    }

    /** Removes an object from the collection immutably */
    private static ObjectCollection removeImmutable(ObjectCollection objects, int index) {
        ObjectCollection out = objects.duplicateShallow();
        out.remove(index);
        return out;
    }

    private static ImageInitialization createInitialization(ObjectCollection others)
            throws OperationFailedException {

        SharedObjects sharedObjects =
                new SharedObjects(
                        new CommonContext(
                                LoggingFixture.suppressedLogger(), Mockito.mock(Path.class)));

        sharedObjects.getOrCreate(ObjectCollection.class).add(ID, () -> others);

        return new ImageInitialization(sharedObjects);
    }

    private static FeatureIntersectingObjects addId(FeatureIntersectingObjects feature) {
        feature.setId(ID);
        return feature;
    }

    private static String combine(String first, String second) {
        return first + "-" + second;
    }
}
