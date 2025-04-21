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

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.plugin.image.task.bean.InputFixture;
import org.anchoranalysis.plugin.image.task.bean.InputFixtureFactory;
import org.anchoranalysis.plugin.image.task.bean.StackIOTestBase;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Montage}.
 *
 * <p>Each test combines six inputs of different sizes and colors.
 *
 * @author Owen Feehan
 */
class MontageTest extends StackIOTestBase {

    /** We don't test the labelled output as fonts vary on windows and linux. */
    private static final List<String> FILENAMES_TO_COMPARE =
            Arrays.asList(Montage.OUTPUT_UNLABELLED + ".png");

    static {
        BeanInstanceMapFixture.ensureStackDisplayer();
    }

    /** Varying the image location and image size. */
    @Test
    void testVaryBoth() throws OperationFailedException, ImageIOException {
        Montage task = new Montage();
        task.setVaryImageLocation(true);
        doTest(task, "varyBoth");
    }

    /** Varying the image size <b>only</b>. */
    @Test
    void testVaryImageSize() throws OperationFailedException, ImageIOException {
        Montage task = new Montage();
        task.setVaryImageLocation(false);
        task.setVaryImageSize(true);
        doTest(task, "varyImageSize");
    }

    /** Varying <b>neither</b> the image size nor location. */
    @Test
    void testVaryNeither() throws OperationFailedException, ImageIOException {
        Montage task = new Montage();
        task.setVaryImageLocation(false);
        task.setVaryImageSize(false);
        doTest(task, "varyNeither");
    }

    @SuppressWarnings("unchecked")
    private void doTest(Montage task, String expectedOutputSubdirectory)
            throws ImageIOException, OperationFailedException {

        BeanInstanceMapFixture.check(task);

        InputFixture fixture = InputFixtureFactory.createSixColors();

        new ExecuteTaskHelper()
                .assertExpectedTaskOutputs(
                        (List<StackSequenceInput>) fixture.createInputs(STACK_READER, true),
                        task,
                        directory,
                        "montage/expectedOutput/" + expectedOutputSubdirectory,
                        FILENAMES_TO_COMPARE);
    }
}
