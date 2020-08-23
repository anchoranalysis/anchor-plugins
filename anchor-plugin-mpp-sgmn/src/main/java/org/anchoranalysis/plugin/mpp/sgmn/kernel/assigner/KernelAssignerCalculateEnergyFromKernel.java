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

package org.anchoranalysis.plugin.mpp.sgmn.kernel.assigner;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelWithIdentifier;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.kernelbridge.KernelStateBridge;
import lombok.AllArgsConstructor;

/**
 * @author Owen Feehan
 * @param <S> Optimization state
 * @param <T> Knerel type
 */
@AllArgsConstructor
public class KernelAssignerCalculateEnergyFromKernel<S, T> implements KernelAssigner<S, T> {

    private final KernelStateBridge<S, T> kernelStateBridge;

    @Override
    public void assignProposal(
            OptimizationStep<S, T> optStep, TransformationContext context, KernelWithIdentifier<S> kernel)
            throws KernelCalculateEnergyException {

        try {
            Optional<S> proposalOptional = proposal(optStep, kernel, context);
            optStep.assignProposal(
                    OptionalUtilities.flatMap(
                            proposalOptional,
                            proposal -> kernelStateBridge.kernelToState().transform(proposalOptional, context)
                    ),
                    kernel);

        } catch (OperationFailedException e) {
            throw new KernelCalculateEnergyException("Cannot transform function", e);
        }
    }
    
    private Optional<S> proposal(OptimizationStep<S, T> optStep, KernelWithIdentifier<S> kernel, TransformationContext context) throws KernelCalculateEnergyException, OperationFailedException {
        return kernel.getKernel()
            .makeProposal(
                    kernelStateBridge
                            .stateToKernel()
                            .transform(optStep.getCrnt(), context),
                    context.getKernelCalcContext());    
    }
}
