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

public abstract class ScaleImageTestBase extends StackIOTestBase {

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

    /** Creates the task that will be tested. */
    protected abstract ScaleImage<?> createTask();

    /**
     * The name of the directory in test-resources where the expected-outputs for this test are
     * placed.
     */
    protected abstract String resourcesDirectory();

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
                        new TaskArguments(Optional.of(suggestion)));
        helper.runTaskAndCompareOutputs(
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
