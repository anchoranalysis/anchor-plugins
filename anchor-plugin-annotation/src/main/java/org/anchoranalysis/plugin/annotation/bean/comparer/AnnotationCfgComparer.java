/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.mark.MarkAnnotationReader;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.annotation.mark.MarkAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class AnnotationCfgComparer extends Comparer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> createObjects(
            Path filePathSource, ImageDimensions dimensions, boolean debugMode)
            throws CreateException {

        Path filePath;
        try {
            filePath = filePathGenerator.outFilePath(filePathSource, false);
        } catch (AnchorIOException e1) {
            throw new CreateException(e1);
        }

        MarkAnnotationReader annotationReader = new MarkAnnotationReader(false);
        Optional<MarkAnnotation> annotation;
        try {
            annotation = annotationReader.read(filePath);
        } catch (AnchorIOException e) {
            throw new CreateException(e);
        }

        if (!annotation.isPresent()) {
            return new NotFound<>(filePath, "No annotation exists");
        }

        if (!annotation.get().isAccepted()) {
            return new NotFound<>(filePath, "The annotation is NOT accepted");
        }

        ObjectCollectionWithProperties omwp =
                annotation
                        .get()
                        .getCfg()
                        .calcMask(
                                dimensions,
                                annotation
                                        .get()
                                        .getRegionMap()
                                        .membershipWithFlagsForIndex(
                                                annotation.get().getRegionID()),
                                BinaryValuesByte.getDefault());
        return new Found<>(omwp.withoutProperties());
    }
}
