package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Copies files using whatever prefix is assigned to an input by the {@link OutputManager} as the
 * file-name.
 *
 * <p>An extension is added, based on whatever extension already exists on the file.
 *
 * <p>The prefix must be guaranteed to be unique for each file copied. This is not actively checked.
 *
 * <p>The prefix is usually derived from the name of the input file relative to the input-directory,
 * so behavior is often similar to {@link PreserveName}. However, other naming-schemes are also
 * possible e.g. assigning a sequence of numbers to each input.
 *
 * @author Owen Feehan
 */
public class UseOutputPrefix extends CopyFilesNamingWithoutSharedState {

    @Override
    public Optional<Path> destinationPathRelative(
            Path sourceDirectory,
            Path destinationDirectory,
            File file,
            int index,
            NoSharedState sharedState)
            throws OutputWriteFailedException {

        Optional<String> extension = ExtensionUtilities.extractExtension(file.toString());
        return null;
    }
}
