/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.error.AnchorIOException;

public abstract class CopyFilesNamingOne implements CopyFilesNaming {

    // START BEAN PROPERTIES
    @BeanField private CopyFilesNaming copyFilesNaming;
    // END BEAN PROPERTIES

    @Override
    public void beforeCopying(Path destDir, int totalNumFiles) {
        copyFilesNaming.beforeCopying(destDir, totalNumFiles);
    }

    @Override
    public Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter)
            throws AnchorIOException {
        Optional<Path> pathDelegate =
                copyFilesNaming.destinationPathRelative(sourceDir, destDir, file, iter);
        return OptionalUtilities.flatMap(pathDelegate, this::destinationPathRelative);
    }

    protected abstract Optional<Path> destinationPathRelative(Path pathDelegate)
            throws AnchorIOException;

    @Override
    public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {
        copyFilesNaming.afterCopying(destDir, dummyMode);
    }

    public CopyFilesNaming getCopyFilesNaming() {
        return copyFilesNaming;
    }

    public void setCopyFilesNaming(CopyFilesNaming copyFilesNaming) {
        this.copyFilesNaming = copyFilesNaming;
    }
}
