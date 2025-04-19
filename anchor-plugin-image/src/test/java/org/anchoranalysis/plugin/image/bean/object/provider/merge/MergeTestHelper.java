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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.provider.ProviderFixture;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.anchoranalysis.test.feature.plugins.objects.IntersectingCircleObjectsFixture;

/**
 * Helper class for testing merge operations on object collections.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MergeTestHelper {

    /** Number of intersecting objects in the test collection. */
    private static final int NUMBER_INTERSECTING = 8;

    /** Number of non-intersecting objects in the test collection. */
    private static final int NUMBER_NOT_INTERSECTING = 3;

    /** Expected result when all intersecting objects are merged. */
    public static final int EXPECTED_RESULT_ALL_INTERSECTING_MERGED = NUMBER_NOT_INTERSECTING + 1;

    /** Expected result when the first three objects are not merged. */
    public static final int EXPECTED_RESULT_FIRST_THREE_NOT_MERGING =
            NUMBER_NOT_INTERSECTING + 3 + 1;

    /**
     * Linear intersection (intersects with left and right neighbor) among the first 8 objects, and then 3 more that don't intersect.
     *
     * <pre>i.e. a pattern     a--b--c--d--e--f--g--h i j k   where  --  represents a neighborhood relation</pre>
     *
     * <pre>The sizes of the 11 objects increase i.e. 81, 149, 253, 377, 529, ....., 1653, 1961</pre>
     */
    public static final ObjectCollection OBJECTS_LINEAR_INTERSECTING =
            IntersectingCircleObjectsFixture.generateIntersectingObjects(
                    NUMBER_INTERSECTING, NUMBER_NOT_INTERSECTING, false);

    /**
     * Tests the initialization and execution of a provider of object-masks that results in a number of merged-objects.
     *
     * @param expectedFinalMergeCount the expected size() of the object-masks returned by the provider
     * @param expectedFeatureCalcCount the expected number of times the calc() method is called on the Feature
     * @param expectedCalculationCount the expected number of times the execute() method is called on the Calculation
     * @param provider a provider of object-masks
     * @throws OperationFailedException if an error occurs during the operation
     */
    public static void testProviderOn(
            int expectedFinalMergeCount,
            int expectedFeatureCalcCount,
            int expectedCalculationCount,
            MergeBase provider)
            throws OperationFailedException {

        Logger logger = LoggerFixture.suppressedLogger();

        try {
            ProviderFixture.initProvider(provider, logger);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }

        MockFeatureWithCalculationFixture.executeAndAssertCount(
                expectedFeatureCalcCount,
                expectedCalculationCount,
                () -> {
                    try {
                        ObjectCollection mergedObjects = provider.get();
                        assertEquals(
                                expectedFinalMergeCount,
                                mergedObjects.size(),
                                "final number of merged-objects");
                    } catch (ProvisionFailedException e) {
                        throw new OperationFailedException(e);
                    }
                });
    }
}