package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    /** The string to prepend for the <b>first</b> group, including a trailing separator. */
    public static String GROUP1 = "group1/";

    /** The string to prepend for the <b>second</b> group, including a trailing separator. */
    public static String GROUP2 = "group2/";

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
        return FunctionalList.mapToListWithIndex(
                FILENAMES,
                ImageIOException.class,
                (filename, index) ->
                        new StackSequenceInputFixture(
                                directory,
                                filename,
                                stackReader,
                                addGroupToIdentifier(filename, index),
                                context));
    }

    /** Forms an identifier from a particular filename, by prepending a group. */
    private static Optional<String> addGroupToIdentifier(String filename, int index) {
        return Optional.of(groupIdentifier(index) + filename);
    }

    /** What group to prepend to a particular filename. */
    private static String groupIdentifier(int index) {
        return (index < 2) ? GROUP1 : GROUP2;
    }
}
