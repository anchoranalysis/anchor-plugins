/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.proposer;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernel;

public class KernelProposerOptionDual<T> extends KernelProposerOption<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KernelPosNeg<T> kernelPositive;

    @BeanField @Getter @Setter private KernelPosNeg<T> kernelNegative;

    @BeanField @NonNegative @Getter @Setter private double weightPositive = -1;

    @BeanField @NonNegative @Getter @Setter private double weightNegative = -1;
    // END BEAN PROPERTIES

    @Override
    // Add weighted kernel factories to a list, and returns the total weight
    public double addWeightedKernelFactories(List<WeightedKernel<T>> lst) {

        kernelPositive.setProbPos(weightPositive);
        kernelPositive.setProbNeg(weightNegative);

        kernelNegative.setProbPos(weightNegative);
        kernelNegative.setProbNeg(weightPositive);

        lst.add(new WeightedKernel<T>(kernelPositive, weightPositive));
        lst.add(new WeightedKernel<T>(kernelNegative, weightNegative));
        return getWeightPositive() + getWeightNegative();
    }
}
