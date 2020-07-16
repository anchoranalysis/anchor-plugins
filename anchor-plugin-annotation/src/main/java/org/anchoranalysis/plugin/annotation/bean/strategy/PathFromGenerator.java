/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.nio.file.Path;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class PathFromGenerator {

    /** The debug-mode for everything that isn't the main input */
    private static final boolean DEBUG_MODE_NON_INPUT = false;

    private PathFromGenerator() {}

    public static Path derivePath(FilePathGenerator generator, Path pathForBinding)
            throws AnchorIOException {
        return generator.outFilePath(pathForBinding, DEBUG_MODE_NON_INPUT);
    }
}
