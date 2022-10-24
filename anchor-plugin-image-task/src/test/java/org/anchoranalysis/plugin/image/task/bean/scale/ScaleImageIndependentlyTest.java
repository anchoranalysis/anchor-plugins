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
package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.arguments.TaskArguments;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestionFactory;
import org.anchoranalysis.image.core.dimensions.size.suggestion.SuggestionFormatException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.plugin.image.bean.scale.ToSuggested;
import org.anchoranalysis.plugin.image.task.bean.InputFixture;
import org.anchoranalysis.plugin.image.task.bean.InputFixtureFactory;
import org.anchoranalysis.plugin.image.task.bean.StackIOTestBase;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.Test;

class ScaleImageIndependentlyTest extends StackIOTestBase {

    @Test
    void testFixedWidth()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTest(false, "fixedWidth", ImageSizeSuggestionFactory.create("800x"));
    }

    @Test
    void testFixedHeight()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("fixedHeight", ImageSizeSuggestionFactory.create("x200"));
    }

    @Test
    void testFixedWidthAndHeight()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("fixedWidthAndHeight", ImageSizeSuggestionFactory.create("100x200"));
    }

    @Test
    void testPreserveAspectRatio()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("preserveAspectRatio", ImageSizeSuggestionFactory.create("100x200+"));
    }

    @Test
    void testDownscale()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("downscale", ImageSizeSuggestionFactory.create("0.5"));
    }

    @Test
    void testUpscale()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("upscale", ImageSizeSuggestionFactory.create("1.5"));
    }

    /** Do the test on both binary (binary mask) and non-binary images (RGB). */
    private void doTestBoth(String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {
        doTest(false, expectedOutputSubdirectory, suggestion);
        doTest(true, expectedOutputSubdirectory, suggestion);
    }

    @SuppressWarnings("unchecked")
    private void doTest(
            boolean binary, String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {

        ScaleImageIndependently task = new ScaleImageIndependently();
        task.setBinary(binary);

        task.setScaleCalculator(new ToSuggested());

        BeanInstanceMapFixture.check(task);

        InputFixture fixture =
                binary
                        ? InputFixtureFactory.createSixBinaryMasks()
                        : InputFixtureFactory.createSixColors();

        String binarySubdirectory = binary ? "binary" : "nonBinary";

        ExecuteTaskHelper helper =
                new ExecuteTaskHelper(
                        Optional.of(ScaleImage.OUTPUT_SCALED),
                        new TaskArguments(Optional.of(suggestion)));
        helper.runTaskAndCompareOutputs(
                (List<StackSequenceInput>) fixture.createInputs(STACK_READER, false),
                task,
                directory,
                "scaleImageIndependently/expectedOutput/"
                        + binarySubdirectory
                        + "/"
                        + expectedOutputSubdirectory,
                fixture.getFilesNamesWithExtension());
    }
}
