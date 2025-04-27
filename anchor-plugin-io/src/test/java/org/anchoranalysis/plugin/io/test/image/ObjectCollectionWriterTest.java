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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.output.outputter.BindFailedException;
import org.anchoranalysis.test.LoggerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Writes an object-collection to the filesystem, then reads it back again, and makes sure it is
 * identical
 *
 * @author Owen Feehan
 */
class ObjectCollectionWriterTest {

    @TempDir Path directory;

    private ObjectCollectionFixture fixture = new ObjectCollectionFixture();

    @BeforeEach
    void setUp() {
        RegisterBeanFactories.registerAllPackageBeanFactories();
    }

    @Test
    void testHdf5()
            throws SetOperationFailedException,
                    DeserializationFailedException,
                    BindFailedException {
        ObjectCollection objects = fixture.createMockObjects(2, 7);
        writeObjects(objects, directory, generator(true, false));

        ObjectCollection objectsRead =
                readObjects(outputPathExpected(directory), LoggerFixture.suppressedLogger());

        assertEquals(objects.size(), objectsRead.size(), "Objects size");
        assertTrue(objects.equalsDeep(objectsRead));
    }

    private static Path outputPathExpected(Path path) {
        return path.resolve("objects.h5");
    }
}
