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
		
		CopyContext<AnonymizeSharedState> context = new CopyContext<AnonymizeSharedState>(inputDirectory, outputDirectory, sharedState);
		calculateAndCheckDestinationPath(anonymize, inputs, outputDirectory, context);
    }
	
	/** Create a sub-directory from the temporary directory. */
	private static Path createSubdirectory(Path tempDirectory, String folderName) throws IOException {
		return Files.createDirectories(tempDirectory.resolve(folderName));
	}
	
	/** Calculates the destination path, and asserts it meets expectations. */
	private static void calculateAndCheckDestinationPath(Anonymize anonymize, List<FileWithDirectoryInput> inputs, Path outputDirectory, CopyContext<AnonymizeSharedState> context) throws OutputWriteFailedException {
		Optional<Path> destination = anonymize.destinationPath(inputs.get(0).getFile(), new DirectoryWithPrefix(outputDirectory), 0, context);
		assertFalse(destination.isEmpty());
		assertPathEndsWith(destination.get());
	}
	
	/** Asserts the path ends with the expected sub-directory and file-name. */
	private static void assertPathEndsWith(Path path) {
		String withForwardSlashes = path.toString().replace('\\', '/');
		assertTrue(withForwardSlashes.endsWith( DIRECTORY_OUTPUTS + "/0.png"));
	}
}