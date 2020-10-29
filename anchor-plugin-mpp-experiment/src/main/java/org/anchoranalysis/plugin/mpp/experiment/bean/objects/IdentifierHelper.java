package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class IdentifierHelper {

    public static String identifierForPathAsString(DerivePath deriver, Optional<Path> path, boolean debugMode) throws DerivePathException {
        return FilePathToUnixStyleConverter.toStringUnixStyle( identifierForPath(deriver, path, debugMode) );
    }
    
    private static Path identifierForPath(DerivePath deriver, Optional<Path> path, boolean debugMode)
            throws DerivePathException {
        if (!path.isPresent()) {
            throw new DerivePathException(
                    "A binding-path is not present for the input, but is required");
        }

        return deriver != null ? deriver.deriveFrom(path.get(), debugMode) : path.get();
    }
}
