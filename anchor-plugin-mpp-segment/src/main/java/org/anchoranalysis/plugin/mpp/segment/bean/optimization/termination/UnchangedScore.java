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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;

/**
 * Terminate when the <b>score</b> remains unchanged for a certain number of iterations.
 *
 * @author Owen Feehan
 */
public class UnchangedScore extends UnchangedBase {

    // START BEAN PARAMETERS
    /**
     * An exponent of 10 which determines a tolerance, less than which a score will be deemed
     * unchanged compared to another.
     */
    @BeanField @Getter @Setter private int tolerance = -1;
    // END BEAN PARAMETERS

    /** The value after raising {@code 10^exponent}. */
    private double toleranceRaised = -1;

    /** The total energy in the previous step. */
    private double previousScore = 0;

    @Override
    public void initialize() {
        super.initialize();
        this.previousScore = 0;
        this.toleranceRaised = Math.pow(10.0, tolerance);
    }

    @Override
    protected void assignPrevious(double score, int size) {
        this.previousScore = score;
    }

    @Override
    protected boolean isUnchanged(double score, int size) {
        return Math.abs(score - previousScore) < this.toleranceRaised;
    }
}
