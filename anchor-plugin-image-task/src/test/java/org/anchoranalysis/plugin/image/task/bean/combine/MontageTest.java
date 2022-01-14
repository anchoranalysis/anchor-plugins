package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link Montage}.
 *
 * <p>Each test combines six inputs of different sizes and colors.
 *
 * @author Owen Feehan
 */
class MontageTest {

    private static List<String> FILENAMES_TO_COMPARE = Arrays.asList("montage.png");

    /** The respective colors of the six images. */
    private static List<String> FILENAMES =
            Arrays.asList(
                    "blue.png", "red.png", "yellow.png", "green.png", "gray.png", "orange.png");

    // START: Ensure needed instances exist in the default BeanInstanceMap
    private static StackReader stackReader = BeanInstanceMapFixture.ensureStackReader();

    static {
        BeanInstanceMapFixture.ensureStackWriter(false);
        BeanInstanceMapFixture.ensureImageMetadataReader();
        BeanInstanceMapFixture.ensureInterpolator();
    }
    // END: Ensure needed instances exist in the default BeanInstanceMap

    private TestLoaderImage loader =
            new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory());

    @TempDir Path directory;

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

    private void doTest(Montage task, String expectedOutputSubdirectory)
            throws ImageIOException, OperationFailedException {
        Path path = loader.getLoader().resolveTestPath("montage/input/six");
        BeanInstanceMapFixture.check(task);

        // The inputs
        List<StackSequenceInput> inputs =
                createInputs(path, LoggerFixture.suppressedOperationContext());

        ExecuteTaskHelper.runTaskAndCompareOutputs(
                inputs,
                task,
                directory,
                "montage/expectedOutput/" + expectedOutputSubdirectory,
                FILENAMES_TO_COMPARE);
    }

    /** Create an input for each of the {@code #FILENAMES}. */
    private static List<StackSequenceInput> createInputs(Path directory, OperationContext context)
            throws ImageIOException {
        return FunctionalList.mapToList(
                FILENAMES.stream(),
                ImageIOException.class,
                filename ->
                        new StackSequenceInputFixture(directory, filename, stackReader, context));
    }
}
