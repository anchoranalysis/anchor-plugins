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

public class AnnotationComparisonInput<T extends InputFromManager>
        extends InputFromManagerDelegate<T> {

    @Getter private final Tuple2<ComparableSource, ComparableSource> comparers;
    @Getter private final Tuple2<String, String> names;
    @Getter private final StackReader stackReader;

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

    // Uses a boolean flag to multiplex between comparerLeft and comparerRight
    public ComparableSource getComparerMultiplex(boolean left) {
        if (left) {
            return comparers._1();
        } else {
            return comparers._2();
        }
    }

    public T getInput() {
        return getDelegate();
    }
}
