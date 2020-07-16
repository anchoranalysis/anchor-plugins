/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.nio.file.Path;
import org.anchoranalysis.annotation.io.bean.strategy.AnnotatorStrategy;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public abstract class SingleFilePathGeneratorStrategy extends AnnotatorStrategy {

    // START BEAN PROPERTIES
    @BeanField private FilePathGenerator annotationFilePathGenerator;
    // END BEAN PROPERTIES

    @Override
    public Path annotationPathFor(ProvidesStackInput item) throws AnchorIOException {
        return PathFromGenerator.derivePath(
                annotationFilePathGenerator, item.pathForBindingRequired());
    }

    public FilePathGenerator getAnnotationFilePathGenerator() {
        return annotationFilePathGenerator;
    }

    public void setAnnotationFilePathGenerator(FilePathGenerator annotationFilePathGenerator) {
        this.annotationFilePathGenerator = annotationFilePathGenerator;
    }
}
