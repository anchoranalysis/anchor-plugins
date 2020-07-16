/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;

public class StaticFilePath extends FilePath {

    // START BEAN PROPERTIES
    @BeanField private String path;
    // END BEAN PROPERTIES

    @Override
    public Path path(boolean debugMode) throws AnchorIOException {
        return Paths.get(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
