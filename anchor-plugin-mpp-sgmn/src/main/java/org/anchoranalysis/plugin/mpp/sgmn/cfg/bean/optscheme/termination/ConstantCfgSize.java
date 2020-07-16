/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;

/**
 * TODO consider renaming to constant size
 *
 * @author Owen Feehan
 */
public class ConstantCfgSize extends TerminationCondition {

    // BEAN PARAMETERS
    @BeanField private int numRep = -1;
    // END BEAN PARAMETERS

    private int prevSize = 0;
    private int rep = 0;

    public ConstantCfgSize() {
        super();
    }

    @Override
    public boolean continueIterations(int crntIter, double score, int size, MessageLogger logger) {

        // We increase our repetition counter, if the energy total is identical to the last time
        if (size == prevSize) {
            rep++;
        } else {
            rep = 0;
        }

        prevSize = size;

        if (rep < numRep) {
            return true;
        } else {
            logger.log("ConstantCfgSize returned false");
            return false;
        }
    }

    public int getNumRep() {
        return numRep;
    }

    public void setNumRep(int numRep) {
        this.numRep = numRep;
    }
}
