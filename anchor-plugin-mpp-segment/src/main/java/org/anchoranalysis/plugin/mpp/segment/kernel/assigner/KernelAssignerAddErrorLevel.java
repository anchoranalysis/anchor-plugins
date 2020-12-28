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

package org.anchoranalysis.plugin.mpp.segment.kernel.assigner;

import lombok.AllArgsConstructor;
import org.anchoranalysis.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.mpp.segment.kernel.KernelAssigner;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.proposer.KernelWithIdentifier;
import org.anchoranalysis.mpp.segment.optimization.step.OptimizationStep;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;

@AllArgsConstructor
public class KernelAssignerAddErrorLevel<S, T, U> implements KernelAssigner<S, T, U> {

    private KernelAssigner<S, T, U> kernelAssigner;
    private ErrorNode parentErrorNode;

    @Override
    public void assignProposal(
            OptimizationStep<S, T, U> step,
            TransformationContext context,
            KernelWithIdentifier<S, U> kernel)
            throws KernelCalculateEnergyException {

        // Add a sub-level with the Kernel name in the proposer-failure-description
        ErrorNode errorNode = parentErrorNode.add(kernel.getKernel().getName());

        // We assign the proposed marks
        kernelAssigner.assignProposal(step, context.replaceError(errorNode), kernel);
    }
}
