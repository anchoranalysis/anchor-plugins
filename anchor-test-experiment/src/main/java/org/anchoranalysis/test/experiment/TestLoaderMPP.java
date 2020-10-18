/*-
 * #%L
 * anchor-test-experiment
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

package org.anchoranalysis.test.experiment;

import java.nio.file.Path;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.mpp.io.marks.MarkCollectionDeserializer;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;

public class TestLoaderMPP {

    private TestLoader delegate;

    public TestLoaderMPP(TestLoader loader) {
        this.delegate = loader;
    }

    public MarkCollection openMarksFromTestPath(String testPath) {
        Path filePath = delegate.resolveTestPath(testPath);
        return openMarksFromFilePath(filePath);
    }

    public static MarkCollection openMarksFromFilePath(Path filePath) {

        MarkCollectionDeserializer deserializer = new MarkCollectionDeserializer();
        try {
            return deserializer.deserialize(filePath);
        } catch (DeserializationFailedException e) {
            throw new TestDataLoadException(e);
        }
    }
}
