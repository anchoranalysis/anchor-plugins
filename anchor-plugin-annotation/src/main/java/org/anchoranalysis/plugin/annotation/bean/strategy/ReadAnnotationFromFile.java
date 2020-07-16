/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.annotation.io.wholeimage.WholeImageLabelAnnotationReader;
import org.anchoranalysis.annotation.wholeimage.WholeImageLabelAnnotation;
import org.anchoranalysis.io.error.AnchorIOException;

public class ReadAnnotationFromFile {

    private ReadAnnotationFromFile() {}

    public static Optional<WholeImageLabelAnnotation> readCheckExists(Path path) {

        if (path.toFile().exists()) {
            return readAssumeExists(path);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<WholeImageLabelAnnotation> readAssumeExists(Path path) {
        try {
            WholeImageLabelAnnotationReader reader = new WholeImageLabelAnnotationReader();
            return reader.read(path);
        } catch (AnchorIOException exc) {
            // TODO PUT SOME LOGGING HERE
            return Optional.empty();
        }
    }
}
