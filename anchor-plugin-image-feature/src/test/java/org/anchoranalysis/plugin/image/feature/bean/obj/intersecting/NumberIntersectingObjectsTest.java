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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.NumberIntersectingObjects;
import org.junit.Test;

public class NumberIntersectingObjectsTest {

    static final int EXPECTED_FIRST = 1;
    static final int EXPECTED_SECOND = 2;
    static final int EXPECTED_SECOND_LAST = 1;
    static final int EXPECTED_NO_INTERSECTION = 0;

    @Test
    public void testSameSizes()
            throws FeatureCalculationException, InitException, OperationFailedException {
        testForSpecificExpectedValues("sameSize", true);
    }

    @Test
    public void testDifferentSizes()
            throws FeatureCalculationException, InitException, OperationFailedException {
        testForSpecificExpectedValues("differentSize", false);
    }

    // As expected-values are the same, we have a helper function
    private void testForSpecificExpectedValues(String messagePrefix, boolean sameSize)
            throws OperationFailedException, FeatureCalculationException, InitException {
        InteresectingObjectsTestHelper.testPositions(
                messagePrefix,
                new NumberIntersectingObjects(),
                sameSize,
                EXPECTED_FIRST,
                EXPECTED_SECOND,
                EXPECTED_NO_INTERSECTION,
                EXPECTED_NO_INTERSECTION);
    }
}
