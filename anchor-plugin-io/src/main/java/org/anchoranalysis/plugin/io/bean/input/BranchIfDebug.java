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

package org.anchoranalysis.plugin.io.bean.input;

import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Uses one input-manager normally, but a different one if in debug mode.
 *
 * @author Owen Feehan
 * @param <T> input type
 */
public class BranchIfDebug<T extends InputFromManager> extends InputManagerUnary<T> {

    // START BEAN PROPERTIES
    /**
     * If defined, this provides an input to the manager when in debug-mode, rather than than
     * otherwise {@code inputs}.
     *
     * <p>Note that only the first item is ever read from this input-manager.
     */
    @BeanField @OptionalBean @Getter @Setter private InputManager<T> inputDebug;
    // END BEAN PROPERTIES

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParams params)
            throws InputReadFailedException {
        if (params.isDebugModeActivated()) {
            if (inputDebug != null) {
                return inputDebug.inputs(params);
            } else {
                // We pick the first item
                T firstItem = fromDelegate.iterator().next();
                return fromDelegate.withInputs(Collections.singletonList(firstItem));
            }
        }
        return fromDelegate;
    }
}
