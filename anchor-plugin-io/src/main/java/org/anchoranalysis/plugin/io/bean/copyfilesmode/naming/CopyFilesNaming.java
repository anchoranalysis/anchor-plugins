/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.error.AnchorIOException;

public interface CopyFilesNaming {

    void beforeCopying(Path destDir, int totalNumFiles);

    /**
     * Returns the output path (destination to to be copied to) for a given single file
     *
     * @param sourceDir source-directory
     * @param destDir destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @return the absolute-path. if empty, the file should be skipped.
     * @throws AnchorIOException
     */
    default Optional<Path> destinationPath(Path sourceDir, Path destDir, File file, int index)
            throws AnchorIOException {

        Optional<Path> remainder = destinationPathRelative(sourceDir, destDir, file, index);
        return remainder.map(destDir::resolve);
    }

    /**
     * Calculates the relative-output path (to be appended to destDir)
     *
     * @param sourceDir source-directory
     * @param destDir destination-directory
     * @param file file to be copied
     * @param index an increasing sequence of numbers for each file beginning at 0
     * @return the relative-path. if empty, the file should be skipped.
     * @throws AnchorIOException
     */
    Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int index)
            throws AnchorIOException;

    void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException;
}
