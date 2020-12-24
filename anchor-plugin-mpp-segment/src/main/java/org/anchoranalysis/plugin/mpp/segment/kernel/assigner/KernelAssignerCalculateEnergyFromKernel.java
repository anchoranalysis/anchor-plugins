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

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.kernel.KernelAssigner;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.proposer.KernelWithIdentifier;
import org.anchoranalysis.mpp.segment.optimization.step.OptimizationStep;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.kernelbridge.KernelStateBridge;

/**
 * @author Owen Feehan
 * @param <S> Optimization state
 * @param <T> Knerel type
 */
@AllArgsConstructor
public class KernelAssignerCalculateEnergyFromKernel<S, T> implements KernelAssigner<S, T, UpdatableMarksList> {

    private final KernelStateBridge<S, T> kernelStateBridge;

    @Override
    public void assignProposal(
            OptimizationStep<S, T, UpdatableMarksList> optStep,
            TransformationContext context,
            KernelWithIdentifier<S,UpdatableMarksList> kernel)
            throws KernelCalculateEnergyException {

        try {
            Optional<S> proposalOptional = proposal(optStep, kernel, context);
            optStep.assignProposal(
                    OptionalUtilities.flatMap(
                            proposalOptional,
                            proposal ->
                                    kernelStateBridge
                                            .kernelToState()
                                            .transform(proposalOptional, context)),
                    kernel);

        } catch (OperationFailedException e) {
            throw new KernelCalculateEnergyException("Cannot transform function", e);
        }
    }

    private Optional<S> proposal(
            OptimizationStep<S, T, UpdatableMarksList> optStep,
            KernelWithIdentifier<S,UpdatableMarksList> kernel,
            TransformationContext context)
            throws KernelCalculateEnergyException, OperationFailedException {
        return kernel.getKernel()
                .makeProposal(
                        kernelStateBridge.stateToKernel().transform(optStep.getCurrent(), context),
                        context.getKernelCalcContext());
    }
}
