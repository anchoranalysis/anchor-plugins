/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.proposer;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernel;

@NoArgsConstructor
@AllArgsConstructor
public class KernelProposerOptionSingle<T> extends KernelProposerOption<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Kernel<T> kernel = null;

    @BeanField @Getter @Setter private double weight = 0;
    // END BEAN PROPERTIES

    @Override
    public double getWeightPositive() {
        return getWeight();
    }

    @Override
    public double getWeightNegative() {
        return getWeight();
    }

    @Override
    // Add weighted kernel factories to a list, and returns the total weight
    public double addWeightedKernelFactories(List<WeightedKernel<T>> lst) {
        lst.add(new WeightedKernel<T>(kernel, getWeight()));
        return getWeight();
    }
}
