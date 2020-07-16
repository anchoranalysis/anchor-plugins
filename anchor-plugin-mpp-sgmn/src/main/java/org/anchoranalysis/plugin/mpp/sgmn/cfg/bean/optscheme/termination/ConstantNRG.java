/* (C)2020 */
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
