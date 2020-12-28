package org.anchoranalysis.plugin.mpp.segment.bean.optimization.termination;

import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;

/**
 * Parent class for {@link TerminationCondition} that rely on some attribute remaining unchanged for a certain number of iterations.
 * 
 * @author Owen Feehan
  */
public abstract class UnchangedBase extends NumberIterationsBase {
    
    /** The number of repeats so far while then number-of-marks is unchanged. */
    private int repeatsUnchanged = 0;
    
    @Override
    public boolean continueFurther(
            int iteration, double score, int size, MessageLogger logger) {

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
            logger.logFormatted("%s has been unchanged for %d iterations, so terminating.", UnchangedSize.class.getName(), repeatsUnchanged);
            return false;
        }
    }
    
    /** Whether the current value is unchanged compared to the previous value. */
    protected abstract boolean isUnchanged(double score, int size);
    
    /** Assigns the previous value to be remembered for comparison next time. */
    protected abstract void assignPrevious(double score, int size);
}
