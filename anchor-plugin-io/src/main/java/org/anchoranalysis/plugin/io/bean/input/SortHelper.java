/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputsWithDirectory;

/**
 * Helps sort inputs.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SortHelper {

    /**
     * Sorts the inputs that come from a delegate.
     *
     * @param <T> input-type
     * @param fromDelegate the delegate that supplies the inputs.
     * @return a newly created {@link InputsWithDirectory} with identical directory, but with sorted
     *     inputs.
     */
    public static <T extends InputFromManager> InputsWithDirectory<T> sortInputs(
            InputsWithDirectory<T> fromDelegate) {
        List<T> list = new ArrayList<>(fromDelegate.inputs());
        Collections.sort(list, (T o1, T o2) -> o1.identifier().compareTo(o2.identifier()));
        return fromDelegate.withInputs(list);
    }
}
