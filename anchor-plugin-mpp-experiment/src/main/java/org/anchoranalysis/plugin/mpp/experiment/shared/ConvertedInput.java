/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * A converted-input, together with any logged messages associated with it.
 *
 * @author Owen Feehan
 * @param <U> type an input is converted to
 */
@AllArgsConstructor
@Value
public class ConvertedInput<U extends InputFromManager> {

    /** The converted input. */
    @Getter private U conversion;

    /** Any messages written to the log during conversion. */
    private StringBuilder messages;

    /**
     * Logs any messages stored from from conversion.
     *
     * @param logger the logger to log to.
     */
    public void logConversionMessages(MessageLogger logger) {
        String loggedMessages = messages.toString();
        if (!loggedMessages.isEmpty()) {
            logger.log(loggedMessages);
        }
    }
}
