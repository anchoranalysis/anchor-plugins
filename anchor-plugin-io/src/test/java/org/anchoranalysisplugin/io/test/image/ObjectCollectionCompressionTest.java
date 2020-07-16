/* (C)2020 */
package org.anchoranalysisplugin.io.test.image;

import static org.anchoranalysisplugin.io.test.image.HelperReadWriteObjects.*;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.output.bound.BindFailedException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ObjectCollectionCompressionTest {

    // An uncompressed obj-mask-collection
    private static final String PATH_UNCOMPRESSED_OBJECTS = "objectsUncompressed/objects.h5";

    private TestLoaderImageIO testLoader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCompression()
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {

        ObjectCollectionWithSize uncompressed = calcUncompressed(PATH_UNCOMPRESSED_OBJECTS);

        ObjectCollectionWithSize compressed =
                calcCompressed(uncompressed.getObjects(), folder.getRoot().toPath());

        double relativeSize = uncompressed.relativeSize(compressed);

        assertTrue(uncompressed.getObjects().equalsDeep(compressed.getObjects()));

        // We expect compression of approximate 6.05 on this particular example
        assertTrue(relativeSize > 6 && relativeSize < 6.1);
    }

    private ObjectCollectionWithSize calcUncompressed(String pathIn) {

        // Read the object, and write it again, this time compressed
        ObjectCollection objects = testLoader.openObjectsFromTestPath(pathIn);

        long size = fileSizeBytes(testLoader.getTestLoader().resolveTestPath(pathIn));

        return new ObjectCollectionWithSize(objects, size);
    }

    private static ObjectCollectionWithSize calcCompressed(
            ObjectCollection objectsUncompressed, Path root)
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {

        Path pathOut = root.resolve(TEMPORARY_FOLDER_OUT + ".h5");

        ObjectCollection objectsCompressed = writeAndReadAgain(objectsUncompressed, root, pathOut);

        long size = fileSizeBytes(pathOut);

        return new ObjectCollectionWithSize(objectsCompressed, size);
    }

    private static ObjectCollection writeAndReadAgain(
            ObjectCollection objects, Path pathRoot, Path pathOut)
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {
        // Write the objects to the file-system and read again
        writeObjects(objects, pathRoot, generator(true, true));
        return readObjects(pathOut);
    }

    private static long fileSizeBytes(Path testPath) {
        return testPath.toFile().length();
    }
}
