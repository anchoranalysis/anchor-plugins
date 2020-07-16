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
