/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2025 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.input.path.CopyContext;
import org.anchoranalysis.plugin.io.shared.AnonymizeSharedState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AnonymizeTest {

    /** Name of the sub-directory in which <b>inputs</b> are created. */
    private static final String DIRECTORY_INPUTS = "inputs";

    /** Name of the sub-directory in which <b>outputs</b> are created. */
    private static final String DIRECTORY_OUTPUTS = "outputs";

    @Test
    void testRun(@TempDir Path tempDirectory) throws IOException, OutputWriteFailedException {

        Path inputDirectory = createSubdirectory(tempDirectory, DIRECTORY_INPUTS);
        Path outputDirectory = createSubdirectory(tempDirectory, DIRECTORY_OUTPUTS);

        List<FileWithDirectoryInput> inputs = InputsFixture.createInputs(inputDirectory);

        Anonymize anonymize = new Anonymize();
        AnonymizeSharedState sharedState = anonymize.beforeCopying(inputDirectory, inputs);

        CopyContext<AnonymizeSharedState> context =
                new CopyContext<AnonymizeSharedState>(inputDirectory, outputDirectory, sharedState);
        calculateAndCheckDestinationPath(anonymize, inputs, outputDirectory, context);
    }

    /** Create a sub-directory from the temporary directory. */
    private static Path createSubdirectory(Path tempDirectory, String folderName)
            throws IOException {
        return Files.createDirectories(tempDirectory.resolve(folderName));
    }

    /** Calculates the destination path, and asserts it meets expectations. */
    private static void calculateAndCheckDestinationPath(
            Anonymize anonymize,
            List<FileWithDirectoryInput> inputs,
            Path outputDirectory,
            CopyContext<AnonymizeSharedState> context)
            throws OutputWriteFailedException {
        Optional<Path> destination =
                anonymize.destinationPath(
                        inputs.get(0).getFile(),
                        new DirectoryWithPrefix(outputDirectory),
                        0,
                        context);
        assertFalse(destination.isEmpty());
        assertPathContent(destination.get().toString());
    }

    /**
     * Asserts the path contains the expected sub-directory and ends with the expected
     * file-extension.
     */
    private static void assertPathContent(String path) {
        assertTrue(path.contains(DIRECTORY_OUTPUTS));
        assertTrue(path.endsWith(".png"));
    }
}
