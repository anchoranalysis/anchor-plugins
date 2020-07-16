/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;

public class NumberIterations extends TerminationCondition {

    // START BEAN PROPERTIES
    @BeanField private int maxNumber = -1;
    // END BEAN PROPERTIES

    public NumberIterations() {
        // Standard Bean Constructor
    }

    public NumberIterations(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    @Override
    public boolean continueIterations(int crntIter, double score, int size, MessageLogger logger) {
        if (crntIter < maxNumber) {
            return true;
        } else {
            logger.log("NumberIterations returned false");
            return false;
        }
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
}
