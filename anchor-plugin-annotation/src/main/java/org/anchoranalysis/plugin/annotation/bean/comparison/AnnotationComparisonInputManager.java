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

package org.anchoranalysis.plugin.annotation.bean.comparison;

import io.vavr.Tuple;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.exception.InputReadFailedException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;

public class AnnotationComparisonInputManager<T extends InputFromManager>
        extends InputManager<AnnotationComparisonInput<T>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<T> input;

    @BeanField @Getter @Setter private String nameLeft;

    @BeanField @Getter @Setter private String nameRight;

    @BeanField @Getter @Setter private Comparer comparerLeft;

    @BeanField @Getter @Setter private Comparer comparerRight;

    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;
    // END BEAN PROPERTIES

    @Override
    public List<AnnotationComparisonInput<T>> inputs(InputManagerParams params)
            throws InputReadFailedException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), 2)) {

            Iterator<T> itr = input.inputs(params).iterator();

            prm.incrWorker();

            List<T> tempList = new ArrayList<>();
            while (itr.hasNext()) {
                tempList.add(itr.next());
            }

            List<AnnotationComparisonInput<T>> outList;
            try {
                outList =
                        createListInputWithAnnotationPath(
                                tempList, new ProgressReporterOneOfMany(prm));
            } catch (CreateException e) {
                throw new InputReadFailedException("Cannot create inputs (with annotation path)", e);
            }
            prm.incrWorker();

            return outList;
        }
    }

    private List<AnnotationComparisonInput<T>> createListInputWithAnnotationPath(
            List<T> listInputs, ProgressReporter progressReporter) throws CreateException {
        return FunctionalProgress.mapList(
                listInputs,
                progressReporter,
                input ->
                        new AnnotationComparisonInput<>(
                                input,
                                Tuple.of(comparerLeft, comparerRight),
                                Tuple.of(nameLeft, nameRight),
                                stackReader));
    }
}
