/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.objects.ObjectCollectionReader;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;

/**
 * An object-collection to be used to compare against something
 *
 * @author Owen Feehan
 */
public class ObjectComparer extends Comparer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> createObjects(
            Path filePathSource, ImageDimensions dimensions, boolean debugMode)
            throws CreateException {

        try {
            Path objectsPath = filePathGenerator.outFilePath(filePathSource, debugMode);

            if (!objectsPath.toFile().exists()) {
                return new NotFound<>(objectsPath, "No objects exist");
            }

            return new Found<>(ObjectCollectionReader.createFromPath(objectsPath));

        } catch (AnchorIOException | DeserializationFailedException e) {
            throw new CreateException(e);
        }
    }
}
