package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.input.path.CopyContext;

/**
 * Copies files using whatever prefix is assigned to an input by the {@link OutputManager} as the
 * file-name, adding the same extension as the source file.
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
public class OutputPrefixWithExtension extends CopyFilesNamingWithoutSharedState {

    @Override
    public Optional<Path> destinationPathRelative(
            File file,
            DirectoryWithPrefix outputTarget,
            int index,
            CopyContext<NoSharedState> context)
            throws OutputWriteFailedException {

        Optional<String> extension = ExtensionUtilities.extractExtension(file.toString());

        Path prefixRelativeToSource =
                context.getDestinationDirectory().relativize(outputTarget.asPath(false));

        return Optional.of(ExtensionUtilities.appendExtension(prefixRelativeToSource, extension));
    }
}
