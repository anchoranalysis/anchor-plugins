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

import io.vavr.Tuple2;
import lombok.Getter;
import org.anchoranalysis.annotation.io.bean.comparer.ComparableSource;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputFromManagerDelegate;

/**
 * Input for annotation comparison, containing sources to compare and associated metadata.
 *
 * @param <T> type of input from manager.
 */
public class AnnotationComparisonInput<T extends InputFromManager>
        extends InputFromManagerDelegate<T> {

    /** The two {@link ComparableSource}s to compare. */
    @Getter private final Tuple2<ComparableSource, ComparableSource> comparers;

    /** Names associated with the two sources being compared. */
    @Getter private final Tuple2<String, String> names;

    /** The {@link StackReader} to use for reading image stacks. */
    @Getter private final StackReader stackReader;

    /**
     * Creates an annotation comparison input.
     *
     * @param input the input from manager.
     * @param comparers the two {@link ComparableSource}s to compare.
     * @param names names associated with the two sources being compared.
     * @param stackReader the {@link StackReader} to use for reading image stacks.
     */
    public AnnotationComparisonInput(
            T input,
            Tuple2<ComparableSource, ComparableSource> comparers,
            Tuple2<String, String> names,
            StackReader stackReader) {
        super(input);
        this.comparers = comparers;
        this.names = names;
        this.stackReader = stackReader;
    }

    /**
     * Gets one of the two {@link ComparableSource}s based on a boolean flag.
     *
     * @param left if true, returns the left comparer; if false, returns the right comparer.
     * @return the selected {@link ComparableSource}.
     */
    public ComparableSource getComparerMultiplex(boolean left) {
        if (left) {
            return comparers._1();
        } else {
            return comparers._2();
        }
    }

    /**
     * Gets the input from manager.
     *
     * @return the input of type T.
     */
    public T getInput() {
        return getDelegate();
    }
}
