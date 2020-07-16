/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

/**
 * The prefixer uses a combination of a out-path-prefix and the descriptive-name of inputs to create
 * an output prefix
 *
 * @author Owen Feehan
 */
public class FromDescriptiveName extends FilePathPrefixerAvoidResolve {

    @Override
    protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root) {
        Path combined = root.resolve(Paths.get(input.getDescriptiveName()));
        return new FilePathPrefix(combined);
    }
}
