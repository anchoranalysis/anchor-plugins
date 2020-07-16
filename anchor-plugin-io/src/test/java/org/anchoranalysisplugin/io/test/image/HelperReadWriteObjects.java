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
