package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColoredStacksInputFixture {

    /** The respective colors of the six images. */
    private static List<String> FILENAMES =
            Arrays.asList(
                    "blue.png", "red.png", "yellow.png", "green.png", "gray.png", "orange.png");

    /**
     * Create an input corresponding to each of the colored stacks.
     *
     * @param stackReader how to read the images from the file-system.
     * @return a list of inputs.
     */
    public static List<? super StackSequenceInputFixture> createInputs(StackReader stackReader)
            throws ImageIOException {

        TestLoaderImage loader = new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory());

        Path directory = loader.resolveTestPath("montage/input/six");
        OperationContext context = LoggerFixture.suppressedOperationContext();
        return FunctionalList.mapToList(
                FILENAMES.stream(),
                ImageIOException.class,
                filename ->
                        new StackSequenceInputFixture(directory, filename, stackReader, context));
    }
}
