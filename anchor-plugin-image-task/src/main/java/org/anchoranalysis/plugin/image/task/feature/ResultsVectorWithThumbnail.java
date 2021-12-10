/*-
 * #%L
 * anchor-plugin-image-task
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
package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.image.core.stack.DisplayStack;

/**
 * A results-vector with an optional thumbnails associated with it.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@Value
public class ResultsVectorWithThumbnail {

    private CheckedSupplier<ResultsVector, OperationFailedException> resultsVector;
    private CheckedSupplier<Optional<DisplayStack>, OperationFailedException> thumbnail;

    public ResultsVectorWithThumbnail(
            CheckedSupplier<ResultsVector, OperationFailedException> resultsVector) {
        this.resultsVector = resultsVector;
        this.thumbnail = Optional::empty;
    }
}
