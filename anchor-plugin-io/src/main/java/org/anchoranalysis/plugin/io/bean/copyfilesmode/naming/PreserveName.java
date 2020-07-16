/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.error.AnchorIOException;

public class PreserveName implements CopyFilesNaming {

    @Override
    public void beforeCopying(Path destDir, int totalNumFiles) {
        // NOTHING TO DO
    }

    @Override
    public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {
        // NOTHING TO DO
    }

    @Override
    public Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter)
            throws AnchorIOException {
        return Optional.of(NamingUtilities.filePathDiff(sourceDir, file.toPath()));
    }
}
