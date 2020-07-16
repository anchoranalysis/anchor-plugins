/* (C)2020 */
package org.anchoranalysisplugin.io.test.image;

import java.nio.file.Path;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.io.objects.GeneratorHDF5;
import org.anchoranalysis.image.io.objects.GeneratorTIFFDirectory;
import org.anchoranalysis.image.io.objects.ObjectCollectionReader;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.output.bound.BindFailedException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.test.image.io.OutputManagerFixture;
import org.anchoranalysis.test.image.io.TestReaderWriterUtilities;

class HelperReadWriteObjects {

    public static final String TEMPORARY_FOLDER_OUT = "objects";

    public static IterableGenerator<ObjectCollection> generator(boolean hdf5, boolean compression) {
        if (hdf5) {
            return new GeneratorHDF5(compression);
        } else {
            return new GeneratorTIFFDirectory();
        }
    }

    public static void writeObjects(
            ObjectCollection objects, Path path, IterableGenerator<ObjectCollection> generator)
            throws SetOperationFailedException, BindFailedException {
        generator.setIterableElement(objects);

        BoundOutputManagerRouteErrors outputManager =
                OutputManagerFixture.outputManagerForRouterErrors(path);

        outputManager.getWriterAlwaysAllowed().write("objects", () -> generator.getGenerator());
    }

    public static ObjectCollection readObjects(Path path) throws DeserializationFailedException {

        TestReaderWriterUtilities.ensureRasterReader();

        return ObjectCollectionReader.createFromPath(path);
    }
}
