/* (C)2020 */
package org.anchoranalysisplugin.io.test.image;

import static org.anchoranalysisplugin.io.test.image.HelperReadWriteObjects.*;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.output.bound.BindFailedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Writes an object-collection to the file-system, then reads it back again, and makes sure it is
 * identical
 *
 * @author feehano
 */
public class ObjectCollectionWriterTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    private ObjectCollectionFixture fixture = new ObjectCollectionFixture();

    @Before
    public void setUp() {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    @Test
    public void testHdf5()
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {
        testWriteRead(true);
    }

    @Test
    public void testTIFFDirectory()
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {
        testWriteRead(false);
    }

    private void testWriteRead(boolean hdf5)
            throws SetOperationFailedException, DeserializationFailedException,
                    BindFailedException {
        Path path = folder.getRoot().toPath();

        ObjectCollection objects = fixture.createMockObjects(2, 7);
        writeObjects(objects, path, generator(hdf5, false));

        ObjectCollection objectsRead = readObjects(outputPathExpected(hdf5, path));

        assertTrue(objects.equalsDeep(objectsRead));
    }

    private static Path outputPathExpected(boolean hdf5, Path path) {
        if (hdf5) {
            return path.resolve("objects.h5");
        } else {
            return path.resolve("objects");
        }
    }
}
