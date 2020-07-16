/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.CommonContext;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
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
     * @throws FeatureCalcException
     * @throws InitException
     */
    public static void testPositions(
            String messagePrefix,
            FeatureIntersectingObjects feature,
            boolean sameSize,
            int expectedFirst,
            int expectedSecond,
            int expectedSecondLast,
            int expectedLast)
            throws OperationFailedException, FeatureCalcException, InitException {

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
     * @throws InitException
     * @throws FeatureCalcException
     * @throws OperationFailedException
     */
    private static void assertFeatureIndexInt(
            String message,
            FeatureIntersectingObjects feature,
            ObjectCollection objects,
            int index,
            int expectedResult)
            throws OperationFailedException, FeatureCalcException, InitException {

        // We take the second object in the collection, as one that should intersect with 2 others
        ObjectMask objectMask = objects.get(index);
        ObjectCollection others = removeImmutable(objects, index);

        // We take the final objection the collection , as one

        FeatureTestCalculator.assertIntResult(
                message,
                addId(feature),
                new FeatureInputSingleObject(objectMask, CircleObjectFixture.nrgStack()),
                Optional.of(createInitParams(others).getSharedObjects()),
                expectedResult);
    }

    /** Removes an object from the collection immutably */
    private static ObjectCollection removeImmutable(ObjectCollection objects, int index) {
        ObjectCollection out = objects.duplicateShallow();
        out.remove(index);
        return out;
    }

    private static ImageInitParams createInitParams(ObjectCollection others)
            throws OperationFailedException {

        SharedObjects so =
                new SharedObjects(
                        new CommonContext(
                                LoggingFixture.suppressedLogErrorReporter(),
                                Mockito.mock(Path.class)));

        so.getOrCreate(ObjectCollection.class).add(ID, () -> others);

        return new ImageInitParams(so);
    }

    private static FeatureIntersectingObjects addId(FeatureIntersectingObjects feature) {
        feature.setId(ID);
        return feature;
    }

    private static String combine(String first, String second) {
        return first + "-" + second;
    }
}
