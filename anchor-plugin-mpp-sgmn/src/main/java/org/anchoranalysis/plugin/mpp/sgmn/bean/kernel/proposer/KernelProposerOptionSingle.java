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

package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.proposer;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.segment.bean.kernel.Kernel;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.segment.kernel.proposer.WeightedKernel;

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
