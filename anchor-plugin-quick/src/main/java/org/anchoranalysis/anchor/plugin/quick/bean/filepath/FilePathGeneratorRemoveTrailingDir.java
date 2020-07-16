/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class FilePathGeneratorRemoveTrailingDir extends FilePathGenerator {

    // START BEAN PROPERTIES
    @BeanField private FilePathGenerator filePathGenerator;

    // If non-zero, n trailing directories are removed from the end
    @BeanField private int trimTrailingDirectory = 0;

    // Do not apply the trim operation to the first n dirs
    @BeanField private int skipFirstTrim = 0;
    // END BEAN PROPERTIES

    @Override
    public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
        Path path = filePathGenerator.outFilePath(pathIn, debugMode);

        if (trimTrailingDirectory > 0) {
            return removeNTrailingDirs(path, trimTrailingDirectory, skipFirstTrim);
        } else {
            return path;
        }
    }

    private Path removeNTrailingDirs(Path path, int n, int skipFirstTrim) throws AnchorIOException {
        PathTwoParts pathDir = new PathTwoParts(path);

        for (int i = 0; i < skipFirstTrim; i++) {
            pathDir.moveLastDirToRest();
        }

        for (int i = 0; i < n; i++) {
            pathDir.removeLastDir();
        }

        return pathDir.combine();
    }

    public FilePathGenerator getFilePathGenerator() {
        return filePathGenerator;
    }

    public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
        this.filePathGenerator = filePathGenerator;
    }

    public int getTrimTrailingDirectory() {
        return trimTrailingDirectory;
    }

    public void setTrimTrailingDirectory(int trimTrailingDirectory) {
        this.trimTrailingDirectory = trimTrailingDirectory;
    }

    public int getSkipFirstTrim() {
        return skipFirstTrim;
    }

    public void setSkipFirstTrim(int skipFirstTrim) {
        this.skipFirstTrim = skipFirstTrim;
    }
}
