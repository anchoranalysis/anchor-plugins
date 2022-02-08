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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.bean.grouped.histogram.ExportImageHistograms;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ExportImageHistograms}.
 *
 * @author Owen Feehan
 */
class ExportImageHistogramsTest extends GroupedStackTestBase {

    private static List<String> FILENAMES_TO_COMPARE =
            Arrays.asList("stack00.csv", "stack01.csv", "stack02.csv");

    /** Grouped inputs are placed inside this directory. */
    private static final String SUM_PREFIX = "sum/";

    /**
     * Do <b>not</b> resize the input images.
     *
     * <p>This means the input images have different sizes.
     */
    @Test
    void testDoNotResize() throws OperationFailedException, ImageIOException {
        doTest(false, false, Optional.empty());
    }

    @Override
    protected GroupedStackBase<?, ?> createTask() {
        return new ExportImageHistograms();
    }

    @Override
    protected List<String> filenamesToCompare(boolean groups) {
        if (groups) {
            List<String> combined = new ArrayList<>(FILENAMES_TO_COMPARE.size() * 3);
            combined.addAll(FILENAMES_TO_COMPARE);
            combined.addAll(
                    prependStrings(
                            SUM_PREFIX + ColoredStacksInputFixture.GROUP1, FILENAMES_TO_COMPARE));
            combined.addAll(
                    prependStrings(
                            SUM_PREFIX + ColoredStacksInputFixture.GROUP2, FILENAMES_TO_COMPARE));
            return combined;
        } else {
            return FILENAMES_TO_COMPARE;
        }
    }

    @Override
    protected String subdirectoryResized() {
        return "exportImageHistograms/expectedOutputResized/";
    }

    @Override
    protected String subdirectoryNotResized() {
        return "exportImageHistograms/expectedOutputNotResized/";
    }
}
