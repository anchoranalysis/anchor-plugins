/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.image.bean.channel.aggregator.MeanProjection;
import org.anchoranalysis.plugin.image.task.bean.InputFixture;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link AggregateChannelTask}.
 *
 * @author Owen Feehan
 */
class AggregateChannelTaskTest extends GroupedStackTestBase {

    private static final String OUTPUT_NAME = "someOutput";

    private static List<String> FILENAMES_TO_COMPARE =
            Arrays.asList("stack00.png", "stack01.png", "stack02.png");

    /**
     * Do <b>not</b> resize the input images.
     *
     * <p>This means the input images have different sizes.
     */
    @Test
    void testDoNotResize() throws OperationFailedException, ImageIOException {
        assertThrows(OperationFailedException.class, () -> doTest(false, false, Optional.empty()));
    }

    @Override
    protected GroupedStackBase<?, ?> createTask() {
        AggregateChannelTask task = new AggregateChannelTask();
        task.setAggregator(new MeanProjection<>());
        task.setOutputName(OUTPUT_NAME);
        return task;
    }

    @Override
    protected List<String> filenamesToCompare(boolean groups) {
        if (groups) {
            List<String> combined = new ArrayList<>(FILENAMES_TO_COMPARE.size() * 2);
            combined.addAll(prependStrings(InputFixture.GROUP1, FILENAMES_TO_COMPARE));
            combined.addAll(prependStrings(InputFixture.GROUP2, FILENAMES_TO_COMPARE));
            return combined;
        } else {
            return FILENAMES_TO_COMPARE;
        }
    }

    @Override
    protected String subdirectoryResized() {
        return "aggregateChannel/expectedOutput/";
    }

    @Override
    protected String subdirectoryNotResized() {
        // This is irrelevant, as any paths that lead here throw an exception first.
        return "aggregateChannel/expectedOutput/";
    }
}
