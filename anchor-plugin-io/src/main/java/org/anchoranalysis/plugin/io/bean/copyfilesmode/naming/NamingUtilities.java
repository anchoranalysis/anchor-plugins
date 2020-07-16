/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.filepath.prefixer.PathDifferenceFromBase;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class NamingUtilities {

    public static Path filePathDiff(Path baseFolderPath, Path filePath) throws AnchorIOException {
        PathDifferenceFromBase filePathDiff =
                PathDifferenceFromBase.differenceFrom(baseFolderPath, filePath);
        return filePathDiff.combined();
    }

    // Converts a path to a string, making sure it's first UNIX style
    public static String convertToString(Path path) {
        return FilePathToUnixStyleConverter.toStringUnixStyle(path);
    }
}
