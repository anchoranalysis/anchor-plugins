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

package org.anchoranalysis.plugin.image.feature.object.calculation.single;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.object.CutOffCornersObjectFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculateIncrementalOperationMapTest {

    private static final EnergyStack ENERGY_STACK = EnergyStackFixture.create(true, true);

    private static final FeatureInputSingleObject INPUT =
            new FeatureInputSingleObject(mock(ObjectMask.class), ENERGY_STACK);

    @Spy
    private CalculateIncrementalOperationMap mockMap =
            spy(CalculateIncrementalOperationMapFixture.class);

    @BeforeEach
    void setup() throws OperationFailedException {
        // An arbitrary object
        when(mockMap.applyOperation(any(), any(), anyBoolean()))
                .thenReturn(new CutOffCornersObjectFixture(ENERGY_STACK.dimensions()).create1());
    }

    @Test
    void testInsertingAndAppending() throws OperationFailedException, FeatureCalculationException {

        int first = 8;
        int additional = 3;
        int lessThanFirst = first - 2;

        // Seek a number of objects, where nothing already exists in the cache
        callMockAndVerify(INPUT, first, 1, first);

        // Seek an additional number more, given that NUM_FIRST already exists
        callMockAndVerify(INPUT, first + additional, 2, first + additional);

        // Seek a number we already know is in the cache
        callMockAndVerify(INPUT, lessThanFirst, 2, first + additional);
    }

    @Test
    void testInvalidate() throws FeatureCalculationException {
        // Grow to an initial number
        int numInitial = 6;
        mockMap.getOrCalculate(INPUT, numInitial);
        assertStoredCount(numInitial);

        // Invalidate and check that all objects are gone
        mockMap.invalidate();
        assertStoredCount(0);
    }

    private void assertStoredCount(int expectedNumItemsStored) {
        assertEquals(expectedNumItemsStored, mockMap.numberItemsCurrentlyStored());
    }

    private void callMockAndVerify(
            FeatureInputSingleObject input, int key, int executeTimes, int expectedNumItemsInCache)
            throws FeatureCalculationException, OperationFailedException {
        mockMap.getOrCalculate(input, key);
        verifyMethodCalls(executeTimes, expectedNumItemsInCache);
        assertStoredCount(expectedNumItemsInCache);
    }

    private void verifyMethodCalls(int executeTimes, int applyOperationTimes)
            throws FeatureCalculationException, OperationFailedException {
        verify(mockMap, times(executeTimes)).execute(any(), any());
        verify(mockMap, times(applyOperationTimes)).applyOperation(any(), any(), anyBoolean());
    }

    /**
     * Constructor with zero parameters for CalculateIncrementalOperationMap to facilitate mocking
     */
    private abstract static class CalculateIncrementalOperationMapFixture
            extends CalculateIncrementalOperationMap {

        public CalculateIncrementalOperationMapFixture() {
            super(true);
        }
    }
}
