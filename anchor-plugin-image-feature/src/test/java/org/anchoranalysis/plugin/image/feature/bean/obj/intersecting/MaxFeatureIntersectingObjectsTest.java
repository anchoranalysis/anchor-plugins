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

import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.MaxFeatureIntersectingObjects;
import org.junit.jupiter.api.Test;

class MaxFeatureIntersectingObjectsTest {

    private static final FeatureIntersectingObjects FEATURE =
            FeatureHelper.createWithFeature(new MaxFeatureIntersectingObjects());

    /**
     * We expect always the same number of pixels apart from the last object which has 0 neighbors
     *
     * @throws FeatureCalculationException
     * @throws InitializeException
     * @throws OperationFailedException
     */
    @Test
    void testSameSizes() throws FeatureCalculationException, OperationFailedException {
        IntersectingObjectsTestHelper.testPositions(
                "sameSize",
                FEATURE,
                true,
                FeatureHelper.EXPECTED_NUMBER_PIXELS_FIRST_CIRCLE,
                FeatureHelper.EXPECTED_NUMBER_PIXELS_FIRST_CIRCLE,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS);
    }

    /**
     * We expect a growing number of pixels apart from the last object which has 0 neighbors
     *
     * @throws FeatureCalculationException
     * @throws InitializeException
     * @throws OperationFailedException
     */
    @Test
    void testDifferentSizes() throws FeatureCalculationException, OperationFailedException {
        IntersectingObjectsTestHelper.testPositions(
                "differentSize",
                FEATURE,
                false,
                FeatureHelper.EXPECTED_NUMBER_PIXELS_FIRST_CIRCLE,
                FeatureHelper.EXPECTED_NUMBER_PIXELS_SECOND_CIRCLE,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS);
    }
}
