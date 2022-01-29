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

import java.util.ListIterator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParameters;

/**
 * Helps limit elements in a list of inputs.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LimitHelper {

    /**
     * Limits then number of elements in a list of inputs, by accepting the first {@code
     * keepNumberItems} elements until the limit is reached.
     *
     * <p>A log message is also written, if this occurs.
     *
     * <p>If the number of inputs is less than {@code maxNumberItems} no change occurs to {@code
     * list} and no log message is written.
     *
     * <p>Note that {@code list} is manipulated in-place.
     *
     * @param <T> element-type in list.
     * @param list the list to be possibly be cropped.
     * @param keepNumberItems the (maximum) number of items to keep on the list.
     * @param totalNumberItems the (maximum) the total number of items in the list.
     * @param parameters the {@link InputManager} parameters.
     */
    public static <T> void limitInputsIfNecessary(
            ListIterator<T> iterator,
            int keepNumberItems,
            int totalNumberItems,
            InputManagerParameters parameters) {
        MessageLogger logger = parameters.getLogger().messageLogger();

        int count = 0;

        while (iterator.hasNext()) {
            iterator.next();
            if (count == keepNumberItems) {
                logger.logFormatted(
                        "Limiting the number of inputs, by accepting only the first %d inputs from %d.",
                        keepNumberItems, totalNumberItems);
                logger.logEmptyLine();
            }
            if (count >= keepNumberItems) {
                iterator.remove();
            }
            count++;
        }
    }
}
