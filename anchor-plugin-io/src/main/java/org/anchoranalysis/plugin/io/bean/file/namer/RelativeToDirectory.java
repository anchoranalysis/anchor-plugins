package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.namer.FileNamerIndependent;

/**
 * Constructs a name by finding the relative-path between the file and the input-directory.
 *
 * <p>If no input-directory exists, a name cannot be constructed.
 *
 * <p>Any directory seperator in the he path is always expressed with forward-slashes, even if
 * backslashes are used by the operating system.
 *
 * @author Owen Feehan
 */
public class RelativeToDirectory extends FileNamerIndependent {

    @Override
    protected String deriveName(File file, Optional<Path> inputDirectory, int index)
            throws CreateException {

        if (inputDirectory.isPresent()) {
            Path inputDirectoryAbsolute = makeAbsoluteNormalized(inputDirectory.get());
            Path fileAbsolute = makeAbsoluteNormalized(file.toPath());
            return FilePathToUnixStyleConverter.toStringUnixStyle(
                    inputDirectoryAbsolute.relativize(fileAbsolute));
        } else {
            throw new CreateException(
                    "Cannot derive a name as no input-directory is defined, as is required.");
        }
    }

    private static Path makeAbsoluteNormalized(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
