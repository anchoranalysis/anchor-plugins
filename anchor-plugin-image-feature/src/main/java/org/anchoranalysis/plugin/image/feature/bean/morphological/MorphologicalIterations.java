package org.anchoranalysis.plugin.image.feature.bean.morphological;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Specifies a certain number of iterations of the morphological operations of dilation and erosion 
 * 
 * @author Owen Feehan
 *
 */
@EqualsAndHashCode(callSuper=false)
public class MorphologicalIterations extends AnchorBean<MorphologicalIterations> {

    // START BEAN PROPERTIES
    /** Number of times to perform morphological dilation */
    @BeanField @NonNegative @Getter @Setter private int iterationsDilation = 0;

    /** Number of times to perform morphological erosion */
    @BeanField @NonNegative @Getter @Setter private int iterationsErosion = 0;

    /** Whether to perform the morphological dimensions in 3D or 2D */
    @BeanField @Getter @Setter private boolean do3D = true;
    // END BEAN PROPERTIES

    /**
     * Checks that at least one of {@code iterationsDilation} and {@code iterationsErosion} is more than 0 
     */
    public boolean isAtLeastOnePositive() {
        return iterationsDilation > 0 || iterationsErosion > 0;
    }
    
    /** A string that uniquely identifies all properties in the bean, but friendly to humans */
    public String describePropertiesFriendly() {
        return String.format(
                "iterationsDilation=%d,iterationsErosion=%d,do3D=%s",
                iterationsDilation,
                iterationsErosion,
                do3D ? "true" : "false"
                );
    }
    
    /** A string that uniquely identifies all properties in the bean, but NOT friendly to humans */
    public String uniquelyIdentifyAllProperties() {
         return iterationsDilation + "_" + iterationsErosion + "_" + do3D;
    }
    
    /** Copies the bean setting an new value for {@code iterationsDilation} */
    public MorphologicalIterations copyChangeIterationsDilation( int iterationsDilationsToAssign ) {
        MorphologicalIterations duplicated = duplicateBean();
        duplicated.setIterationsDilation(iterationsDilationsToAssign);
        return duplicated;
    }
}
