/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.comparison;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.input.InputFromManager;
import org.apache.commons.lang3.tuple.Pair;

public class AnnotationComparisonInput<T extends InputFromManager> implements InputFromManager {

    private final T inputObject;
    private final Pair<Comparer, Comparer> comparers;
    private final Pair<String, String> names;
    private final RasterReader rasterReader;

    // Names are allowed be null, to indicate they are not defined
    public AnnotationComparisonInput(
            T inputObject,
            Pair<Comparer, Comparer> comparers,
            Pair<String, String> names,
            RasterReader rasterReader) {
        super();
        this.inputObject = inputObject;
        this.comparers = comparers;
        this.names = names;
        this.rasterReader = rasterReader;
    }

    @Override
    public String descriptiveName() {
        return inputObject.descriptiveName();
    }

    @Override
    public Optional<Path> pathForBinding() {
        return inputObject.pathForBinding();
    }

    // Uses a boolean flag to multiplex between comparerLeft and comparerRight
    public Comparer getComparerMultiplex(boolean left) {
        if (left) {
            return comparers.getLeft();
        } else {
            return comparers.getRight();
        }
    }

    @Override
    public void close(ErrorReporter errorReporter) {
        inputObject.close(errorReporter);
    }

    public T getInputObject() {
        return inputObject;
    }

    public Pair<String, String> getNames() {
        return names;
    }

    public Pair<Comparer, Comparer> getComparers() {
        return comparers;
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }
}
