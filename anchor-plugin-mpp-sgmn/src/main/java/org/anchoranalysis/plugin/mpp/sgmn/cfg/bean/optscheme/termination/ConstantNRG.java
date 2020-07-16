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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;

/**
 * TODO consider renaming to ConstantScore
 *
 * @author Owen Feehan
 */
public class ConstantNRG extends TerminationCondition {

    // START BEAN PARAMETERS
    @BeanField private int toleranceLog10 = -1;

    @BeanField @Positive private int numRep = -1;
    // END BEAN PARAMETERS

    private double tolerance = -1;

    private double prevNRG = 0;

    private int rep = 0;

    @Override
    public boolean continueIterations(int crntIter, double score, int size, MessageLogger logger) {

        // We increase our repetition counter, if the energy total is identical to the last time
        if (Math.abs(score - prevNRG) < this.tolerance) {
            rep++;
        } else {
            rep = 0;
        }

        prevNRG = score;

        if (rep < numRep) {
            return true;
        } else {
            logger.logFormatted("ConstantNRG returned false at iter=%d", crntIter);
            return false;
        }
    }

    @Override
    public void init() {
        super.init();
        this.tolerance = Math.pow(10.0, toleranceLog10);
    }

    public int getToleranceLog10() {
        return toleranceLog10;
    }

    public void setToleranceLog10(int toleranceLog10) {
        this.toleranceLog10 = toleranceLog10;
    }

    public int getNumRep() {
        return numRep;
    }

    public void setNumRep(int numRep) {
        this.numRep = numRep;
    }
}
