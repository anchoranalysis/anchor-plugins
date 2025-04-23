/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.io.test.image;

import static org.anchoranalysis.plugin.io.test.image.HelperReadWriteObjects.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.output.outputter.BindFailedException;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ObjectCollectionCompressionTest {

    // An uncompressed obj-mask-collection
    private static final String PATH_UNCOMPRESSED_OBJECTS = "objectsUncompressed/objects.h5";

    private TestLoaderImage loader =
            new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory());

    @TempDir Path directory;

    @Test
    void testCompression()
            throws SetOperationFailedException,
                    DeserializationFailedException,
                    BindFailedException {

        ObjectCollectionWithSize uncompressed = calculateUncompressed(PATH_UNCOMPRESSED_OBJECTS);

        ObjectCollectionWithSize compressed =
                calculateCompressed(uncompressed.getObjects(), directory);

        double relativeSize = uncompressed.relativeSize(compressed);

        assertTrue(uncompressed.getObjects().equalsDeep(compressed.getObjects()));

        // We expect compression of approximate 6.05 on this particular example
        assertTrue(relativeSize > 6 && relativeSize < 6.1);
    }

    private ObjectCollectionWithSize calculateUncompressed(String pathIn) {

        // Read the object, and write it again, this time compressed
        ObjectCollection objects = loader.openObjectsFromTestPath(pathIn);

        long size = fileSizeBytes(loader.resolveTestPath(pathIn));

        return new ObjectCollectionWithSize(objects, size);
    }

    private static ObjectCollectionWithSize calculateCompressed(
            ObjectCollection objectsUncompressed, Path root)
            throws SetOperationFailedException,
                    DeserializationFailedException,
                    BindFailedException {

        Path pathOut = NonImageFileFormat.HDF5.buildPath(root, TEMPORARY_FOLDER_OUT);

        ObjectCollection objectsCompressed = writeAndReadAgain(objectsUncompressed, root, pathOut);

        long size = fileSizeBytes(pathOut);

        return new ObjectCollectionWithSize(objectsCompressed, size);
    }

    private static ObjectCollection writeAndReadAgain(
            ObjectCollection objects, Path pathRoot, Path pathOut)
            throws SetOperationFailedException,
                    DeserializationFailedException,
                    BindFailedException {
        // Write the objects to the filesystem and read again
        writeObjects(objects, pathRoot, generator(true, true));
        return readObjects(pathOut, LoggerFixture.suppressedLogger());
    }

    private static long fileSizeBytes(Path testPath) {
        return testPath.toFile().length();
    }
}
