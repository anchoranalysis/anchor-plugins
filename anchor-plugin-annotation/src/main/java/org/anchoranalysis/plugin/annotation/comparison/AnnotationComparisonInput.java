/* (C)2020 */
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
