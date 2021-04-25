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

package org.anchoranalysis.plugin.io.bean.input.filter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.InputManagerUnary;
import org.anchoranalysis.plugin.io.input.filter.FilterDescriptiveNameEqualsContains;

/**
 * Filters all the input objects for only those with certain types of descriptive-names.
 *
 * <p>Either or both <i>equals</i> or <i>contains</i> conditions are possible
 *
 * @author Owen Feehan
 * @param <T> input-type
 */
public class FilterDescriptiveName<T extends InputFromManager> extends InputManagerUnary<T> {

    // START BEAN PROPERTIES
    /**
     * A descriptive-name must be exactly equal to (case-sensitive) this string. If empty, disabled.
     */
    @BeanField @AllowEmpty @Getter @Setter private String equals = "";

    /** A descriptive-name must contain (case-sensitive) this string. If empty, disabled. */
    @BeanField @AllowEmpty @Getter @Setter private String contains = "";
    // END BEAN PROPERTIES

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParams params)
            throws InputReadFailedException {
        FilterDescriptiveNameEqualsContains filter =
                new FilterDescriptiveNameEqualsContains(equals, contains);

        List<T> inputsFiltered = filter.removeNonMatching(fromDelegate.inputs());
        return fromDelegate.withInputs(inputsFiltered); // Existing collection
    }
}
