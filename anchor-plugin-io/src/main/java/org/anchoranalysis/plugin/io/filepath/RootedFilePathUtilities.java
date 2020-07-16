/* (C)2020 */
package org.anchoranalysis.plugin.io.filepath;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.bean.root.RootPath;
import org.anchoranalysis.io.bean.root.RootPathMap;
import org.anchoranalysis.io.error.AnchorIOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RootedFilePathUtilities {

    public static Path determineNewPath(
            Path pathOrig, String rootName, boolean debugMode, boolean disableDebugMode)
            throws AnchorIOException {

        // If forceServer is selected, we disable debug-mode
        boolean effectiveDebugMode = debugMode;
        if (disableDebugMode) {
            effectiveDebugMode = false;
        }

        RootPath root = RootPathMap.instance().findRoot(rootName, effectiveDebugMode);
        return root.asPath().resolve(pathOrig);
    }
}
