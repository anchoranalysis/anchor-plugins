/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2025 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

class FrequencyMapTest {

    @Test
    void testDescribeEmptyMap() {
        FrequencyMap<String> frequencyMap = new FrequencyMap<>();
        assertEquals("No inputs have been found yet.", frequencyMap.describe("color"));
    }

    @Test
    void testDescribeWithOneKey() {
        FrequencyMap<String> frequencyMap = new FrequencyMap<>();
        frequencyMap.incrementCount("red");
        assertEquals("with uniform color = red", frequencyMap.describe("color"));
    }

    @Test
    void testDescribeConsistency() {
        FrequencyMap<String> frequencyMap = new FrequencyMap<>();
        frequencyMap.incrementCount("red");
        frequencyMap.incrementCount("blue");
        frequencyMap.incrementCount("green");

        String firstDescription = frequencyMap.describe("color");
        String secondDescription = frequencyMap.describe("color");
        String thirdDescription = frequencyMap.describe("color");

        assertEquals(firstDescription, secondDescription);
        assertEquals(secondDescription, thirdDescription);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        FrequencyMap<String> frequencyMap = new FrequencyMap<>();
        int numThreads = 10;
        int iterationsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(numThreads);

        Runnable task =
                () -> {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        frequencyMap.incrementCount("key" + (i % 3));
                        frequencyMap.describe("item");
                    }
                    latch.countDown();
                };

        for (int i = 0; i < numThreads; i++) {
            new Thread(task).start();
        }

        latch.await();

        String description = frequencyMap.describe("item");
        assertTrue(description.startsWith("with diverse items:"));
        assertTrue(description.contains("key0"));
        assertTrue(description.contains("key1"));
        assertTrue(description.contains("key2"));
        assertEquals("with diverse items: key0(3340) key1(3330) key2(3330)", description);
    }
}
