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

import java.util.ListIterator;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Excludes all inputs whose identifiers match a regular expression.
 *
 * @author Owen Feehan
 * @param <T> input-type.
 */
public class Exclude<T extends InputFromManager> extends InputManagerUnary<T> {

    // START BEAN PROPERTIES
    /** A regular-expression to be matched against the identifiers of inputs. */
    @BeanField @Getter @Setter private RegEx regEx;

    // END BEAN PROPERITES

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParameters parameters)
            throws InputReadFailedException {

        ListIterator<T> itr = fromDelegate.listIterator();
        while (itr.hasNext()) {

            if (regEx.hasMatch(itr.next().identifier())) {
                itr.remove();
            }
        }

        return fromDelegate;
    }
}
