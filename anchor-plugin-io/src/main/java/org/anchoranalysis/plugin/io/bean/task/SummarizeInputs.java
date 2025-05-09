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

package org.anchoranalysis.plugin.io.bean.task;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.image.io.stack.input.ImageMetadataInput;
import org.anchoranalysis.io.input.InputFromManager;

public class SummarizeInputs<T extends InputFromManager> extends SummarizeBase<T, T> {

    @Override
    protected T extractObjectForSummary(T input) {
        return input;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        // Only request conversion to ImageMetadataInput if necessary.
        try {
            if (getSummarizer().requiresImageMetadata()) {
                return new InputTypesExpected(ImageMetadataInput.class);
            } else {
                return new InputTypesExpected(InputFromManager.class);
            }
        } catch (OperationFailedException e) {
            // If it cannot be established assume, that ImageMetdataInput is needed
            return new InputTypesExpected(ImageMetadataInput.class);
        }
    }
}
