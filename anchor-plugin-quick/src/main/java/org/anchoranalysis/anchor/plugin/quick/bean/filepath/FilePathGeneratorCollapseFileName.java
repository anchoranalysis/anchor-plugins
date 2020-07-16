/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.filepath;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.apache.commons.io.FilenameUtils;

/**
 * Takes a file-path of form somedir/somename.ext and converts to somedir.ext
 *
 * @author Owen Feehan
 */
public class FilePathGeneratorCollapseFileName extends FilePathGenerator {

    // START BEAN FIELDS
    @BeanField private FilePathGenerator filePathGenerator;
    // END BEAN FIELDS

    @Override
    public Path outFilePath(Path pathIn, boolean debugMode) throws AnchorIOException {
        Path path = filePathGenerator.outFilePath(pathIn, debugMode);
        return collapse(path);
    }

    private static Path collapse(Path path) throws AnchorIOException {

        PathTwoParts pathDir = new PathTwoParts(path);

        String ext = FilenameUtils.getExtension(pathDir.getSecond().toString());
        if (ext.isEmpty()) {
            return pathDir.getFirst();
        }

        String pathNew = String.format("%s.%s", pathDir.getFirst().toString(), ext);

        return Paths.get(pathNew);
    }

    public FilePathGenerator getFilePathGenerator() {
        return filePathGenerator;
    }

    public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
        this.filePathGenerator = filePathGenerator;
    }
}
