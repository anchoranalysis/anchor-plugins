/*-
 * #%L
 * anchor-plugin-mpp-segment
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
package org.anchoranalysis.plugin.mpp.segment.bean.optimization.termination;

import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;

/**
 * Parent class for {@link TerminationCondition} that rely on some attribute remaining unchanged for
 * a certain number of iterations.
 *
 * @author Owen Feehan
 */
public abstract class UnchangedBase extends NumberIterationsBase {

    /** The number of repeats so far while then number-of-marks is unchanged. */
    private int repeatsUnchanged = 0;

    @Override
    public void initialize() {
        this.repeatsUnchanged = 0;
    }

    @Override
    public boolean continueFurther(int iteration, double score, int size, MessageLogger logger) {

        // We increase our repetition counter, if the energy total is identical to the last time.
        if (isUnchanged(score, size)) {
            repeatsUnchanged++;
        } else {
            repeatsUnchanged = 0;
        }

        assignPrevious(score, size);

        if (repeatsUnchanged < getIterations()) {
            return true;
        } else {
            logger.logFormatted(
                    "%s has been unchanged for %d iterations, so terminating.",
                    UnchangedSize.class.getName(), repeatsUnchanged);
            return false;
        }
    }

    /** Whether the current value is unchanged compared to the previous value. */
    protected abstract boolean isUnchanged(double score, int size);

    /** Assigns the previous value to be remembered for comparison next time. */
    protected abstract void assignPrevious(double score, int size);
}
