/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.termination;

import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.MessageLogger;

/**
 * Terminates after a specific number of iterations.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class NumberIterations extends NumberIterationsBase {

    public NumberIterations(int iterations) {
        super(iterations);
    }

    @Override
    public void init() {
        // NOTHING TO DO
    }

    @Override
    public boolean continueFurther(int iteration, double score, int size, MessageLogger logger) {
        if (iteration < getIterations()) {
            return true;
        } else {
            logger.logFormatted("Terminating as the number of iterations reached %d", iteration);
            return false;
        }
    }
}
