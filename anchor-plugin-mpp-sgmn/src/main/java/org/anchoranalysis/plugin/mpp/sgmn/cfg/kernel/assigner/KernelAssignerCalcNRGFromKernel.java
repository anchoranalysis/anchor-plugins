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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.assigner;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelWithID;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;

/**
 * @author Owen Feehan
 * @param <S> Optimization state
 * @param <T> Knerel type
 */
public class KernelAssignerCalcNRGFromKernel<S, T> implements KernelAssigner<S, T> {

    private KernelStateBridge<S, T> kernelStateBridge;

    public KernelAssignerCalcNRGFromKernel(KernelStateBridge<S, T> kernelStateBridge) {
        super();
        this.kernelStateBridge = kernelStateBridge;
    }

    @Override
    public void assignProposal(
            OptimizationStep<S, T> optStep, TransformationContext context, KernelWithID<S> kid)
            throws KernelCalcNRGException {

        try {
            Optional<S> prop =
                    kid.getKernel()
                            .makeProposal(
                                    kernelStateBridge
                                            .stateToKernel()
                                            .transform(optStep.getCrnt(), context),
                                    context.getKernelCalcContext());

            optStep.assignProposal(
                    OptionalUtilities.flatMap(
                            prop,
                            proposal -> kernelStateBridge.kernelToState().transform(prop, context)),
                    kid);

        } catch (OperationFailedException e) {
            throw new KernelCalcNRGException("Cannot transform function", e);
        }
    }
}
