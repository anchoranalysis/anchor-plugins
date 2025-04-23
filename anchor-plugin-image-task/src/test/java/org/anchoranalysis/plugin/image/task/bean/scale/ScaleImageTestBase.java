/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2023 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

/** Base class for testing {@link ScaleImage} tasks. */
public abstract class ScaleImageTestBase extends StackIOTestBase {

    /**
     * Tests scaling with a fixed width.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testFixedWidth()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTest(false, "fixedWidth", ImageSizeSuggestionFactory.create("800x"));
    }

    /**
     * Tests scaling with a fixed height.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testFixedHeight()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("fixedHeight", ImageSizeSuggestionFactory.create("x200"));
    }

    /**
     * Tests scaling with both fixed width and height.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testFixedWidthAndHeight()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("fixedWidthAndHeight", ImageSizeSuggestionFactory.create("100x200"));
    }

    /**
     * Tests scaling while preserving aspect ratio.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testPreserveAspectRatio()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("preserveAspectRatio", ImageSizeSuggestionFactory.create("100x200+"));
    }

    /**
     * Tests downscaling.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testDownscale()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("downscale", ImageSizeSuggestionFactory.create("0.5"));
    }

    /**
     * Tests upscaling.
     *
     * @throws OperationFailedException if the operation fails
     * @throws ImageIOException if there's an issue with image I/O
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @Test
    void testUpscale()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        doTestBoth("upscale", ImageSizeSuggestionFactory.create("1.5"));
    }

    /**
     * Creates the task that will be tested.
     *
     * @return the created {@link ScaleImage} task
     */
    protected abstract ScaleImage<?> createTask();

    /**
     * The name of the directory in test-resources where the expected-outputs for this test are
     * placed.
     *
     * @return the directory name
     */
    protected abstract String resourcesDirectory();

    /**
     * Do the test on both binary (binary mask) and non-binary images (RGB).
     *
     * @param expectedOutputSubdirectory the subdirectory for expected output
     * @param suggestion the {@link ImageSizeSuggestion} to use
     * @throws ImageIOException if there's an issue with image I/O
     * @throws OperationFailedException if the operation fails
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    private void doTestBoth(String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {
        doTest(false, expectedOutputSubdirectory, suggestion);
        doTest(true, expectedOutputSubdirectory, suggestion);
    }

    /**
     * Performs the actual test for scaling images.
     *
     * @param binary whether to use binary images
     * @param expectedOutputSubdirectory the subdirectory for expected output
     * @param suggestion the {@link ImageSizeSuggestion} to use
     * @throws ImageIOException if there's an issue with image I/O
     * @throws OperationFailedException if the operation fails
     * @throws SuggestionFormatException if there's an issue with the size suggestion format
     */
    @SuppressWarnings("unchecked")
    private void doTest(
            boolean binary, String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {

        ScaleImage<?> task = createTask();
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
                        new TaskArguments(Optional.of(suggestion)),
                        false);
        helper.assertExpectedTaskOutputs(
                (List<StackSequenceInput>) fixture.createInputs(STACK_READER, false),
                task,
                directory,
                resourcesDirectory()
                        + "/expectedOutput/"
                        + binarySubdirectory
                        + "/"
                        + expectedOutputSubdirectory,
                fixture.getFilesNamesWithExtension());
    }
}
