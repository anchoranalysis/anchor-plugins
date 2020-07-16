/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath;

import java.nio.file.Path;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.io.error.AnchorIOException;

public abstract class FilePath extends AnchorBean<FilePath> {

    public abstract Path path(boolean debugMode) throws AnchorIOException;
}
