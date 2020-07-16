/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.io.filepath.RootedFilePathUtilities;

public class RootedFilePath extends FilePath {

    // START BEAN PROPERTIES
    @BeanField private String path;

    @BeanField private String rootName;

    // If TRUE, we will disable debug-mode for this current bean, if debug-mode it's set. Otherwise,
    // there is no impact.
    @BeanField private boolean disableDebugMode = false;
    // END BEAN PROPERTIES

    @Override
    public Path path(boolean debugMode) throws AnchorIOException {
        return RootedFilePathUtilities.determineNewPath(
                Paths.get(path), rootName, debugMode, disableDebugMode);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public boolean isDisableDebugMode() {
        return disableDebugMode;
    }

    public void setDisableDebugMode(boolean disableDebugMode) {
        this.disableDebugMode = disableDebugMode;
    }
}
