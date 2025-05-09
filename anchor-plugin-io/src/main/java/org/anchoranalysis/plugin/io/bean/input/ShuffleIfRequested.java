/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.input;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputContextParameters;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Like {@link Shuffle} if requested in the {@link InputContextParameters} otherwise makes no change
 * to the inputs.
 *
 * <p>If a shuffle is requested, the inputs may be sorted alphabetically (depending on {@code
 * sortIfNotRequested}.
 *
 * <p>The operations are coupled, as sorting makes no sense when shuffling is occurring, but is
 * often desirable otherwise.
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class ShuffleIfRequested<T extends InputFromManager> extends InputManagerUnary<T> {

    // START BEAN PROPERTIES
    /** When true, the inputs are sorted alphabetically, if a shuffle is <b>not</b> requested. */
    @BeanField @Getter @Setter private boolean sortIfNotRequested = false;

    // END BEAN PROPERTIES

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParameters parameters)
            throws InputReadFailedException {

        if (parameters.getInputContext().isShuffle()) {
            ShuffleHelper.shuffleInputs(fromDelegate.inputs(), parameters);
            return fromDelegate;
        } else if (sortIfNotRequested) {
            return SortHelper.sortInputs(fromDelegate);
        } else {
            // Neither shuffling or sorting
            return fromDelegate;
        }
    }
}
