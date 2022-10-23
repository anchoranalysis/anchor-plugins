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
import org.anchoranalysis.plugin.image.task.bean.ColoredStacksInputFixture;
import org.anchoranalysis.plugin.image.task.bean.StackIOTestBase;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.Test;

class ScaleImageIndependentlyTest extends StackIOTestBase {

    /** Fixed width. */
    @Test
    void testFixedWidth()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        ImageSizeSuggestion suggestion = ImageSizeSuggestionFactory.create("800x");
        doTest(false, "fixedWidth", suggestion);
    }

    @SuppressWarnings("unchecked")
    private void doTest(
            boolean binary, String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {

        ScaleImageIndependently task = new ScaleImageIndependently();
        task.setBinary(binary);

        task.setScaleCalculator(new ToSuggested());

        BeanInstanceMapFixture.check(task);

        ExecuteTaskHelper.runTaskAndCompareOutputs(
                (List<StackSequenceInput>)
                        ColoredStacksInputFixture.createInputs(STACK_READER, false),
                true,
                task,
                new TaskArguments(Optional.of(suggestion)),
                directory,
                Optional.of(ScaleImage.OUTPUT_SCALED),
                "scaleImageIndependently/expectedOutput/nonBinary/" + expectedOutputSubdirectory,
                ColoredStacksInputFixture.FILENAMES_WITH_EXTENSION);
    }
}
