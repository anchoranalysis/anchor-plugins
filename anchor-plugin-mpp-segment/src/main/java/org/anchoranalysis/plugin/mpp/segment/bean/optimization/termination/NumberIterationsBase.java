package org.anchoranalysis.plugin.mpp.segment.bean.optimization.termination;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parent class for a {@link TerminationCondition} where the number-of-iterations is a bean-field.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class NumberIterationsBase extends TerminationCondition {

    // BEAN PARAMETERS
    /** The number of iterations for which the number-of-marks must remain constant. */
    @BeanField @Getter @Setter @Positive private int iterations;
    // END BEAN PARAMETERS
}
