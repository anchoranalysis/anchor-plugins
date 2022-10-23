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
package org.anchoranalysis.plugin.image.task.bean;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.plugin.image.task.bean.combine.StackSequenceInputFixture;
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
    private static List<String> FILENAMES_WITHOUT_EXTENSION =
            Arrays.asList("blue", "red", "yellow", "green", "gray", "orange");

    /** The respective colors of the six images. */
    public static List<String> FILENAMES_WITH_EXTENSION =
            FunctionalList.mapToList(FILENAMES_WITHOUT_EXTENSION, filename -> filename + ".png");

    /**
     * Create an input corresponding to each of the colored stacks, with an identifier inferred from
     * the filename, and optionally the group.
     *
     * @param stackReader how to read the images from the file-system.
     * @param includeGroupInIdentifier when true, the group is prefixed (with a separator) into the
     *     identifier. when false, it is omitted entirely.
     * @return a list of inputs, with identifiers determined as above.
     */
    public static List<? super StackSequenceInputFixture> createInputs(
            StackReader stackReader, boolean includeGroupInIdentifier) throws ImageIOException {

        TestLoaderImage loader = new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory());

        Path directory = loader.resolveTestPath("montage/input/six");
        OperationContext context = LoggerFixture.suppressedOperationContext();
        return FunctionalList.mapToListWithIndex(
                FILENAMES_WITH_EXTENSION,
                ImageIOException.class,
                (filename, index) ->
                        new StackSequenceInputFixture(
                                directory,
                                filename,
                                stackReader,
                                addGroupToIdentifier(filename, index, includeGroupInIdentifier),
                                context));
    }

    /**
     * Builds a list of the relative-paths to expected outputs paths produced by a task when the
     * inputs are from this fixture.
     *
     * <p>Each expected output path has the form
     * {GROUP_IDENTIFIER}/{FILENAME_WITHOUT_EXTENSION}_{EXPECTED_OUTPUT_FILENAME} for all {@code
     * expectedOutputFilenames} and {@code FILENAMES_WITHOUT_EXTENSION}
     *
     * @param expectedOutputFilenames a list of filenames that are produced for every output.
     * @return a newly created list of the output-paths.
     */
    public static final List<String> expectedOutputPathsWithGroups(
            List<String> expectedOutputFilenames) {
        List<String> paths =
                new ArrayList<>(
                        FILENAMES_WITHOUT_EXTENSION.size() * expectedOutputFilenames.size());

        // Loop through each input filename and determine its group
        for (int index = 0; index < FILENAMES_WITHOUT_EXTENSION.size(); index++) {
            for (String expectedOutputFilename : expectedOutputFilenames) {
                String group = groupIdentifier(index);
                paths.add(
                        String.format(
                                "%s/%s_%s",
                                group,
                                FILENAMES_WITHOUT_EXTENSION.get(index),
                                expectedOutputFilename));
            }
        }
        return paths;
    }

    /** What group to prepend to a particular filename. */
    private static String groupIdentifier(int index) {
        return (index < 2) ? GROUP1 : GROUP2;
    }

    /** Forms an identifier from a particular filename, by prepending a group. */
    private static Optional<String> addGroupToIdentifier(
            String filename, int index, boolean includeGroupInIdentifier) {
        String identifierWithoutExtension = ExtensionUtilities.removeExtension(filename);
        if (includeGroupInIdentifier) {
            return Optional.of(groupIdentifier(index) + identifierWithoutExtension);
        } else {
            return Optional.of(identifierWithoutExtension);
        }
    }
}
