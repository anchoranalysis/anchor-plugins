package org.anchoranalysis.plugin.image.task.bean.combine;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
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
    private static List<String> FILENAMES_TO_COMPARE =
            Arrays.asList(Montage.OUTPUT_UNLABELLED + ".png");

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

        ExecuteTaskHelper.runTaskAndCompareOutputs(
        		(List<StackSequenceInput>) ColoredStacksInputFixture.createInputs(STACK_READER),
                task,
                directory,
                "montage/expectedOutput/" + expectedOutputSubdirectory,
                FILENAMES_TO_COMPARE);
    }
}
