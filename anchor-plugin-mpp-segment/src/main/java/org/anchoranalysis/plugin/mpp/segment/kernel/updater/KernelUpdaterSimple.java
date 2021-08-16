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

package org.anchoranalysis.plugin.mpp.segment.kernel.updater;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.mark.UpdateMarkSetException;
import org.anchoranalysis.mpp.segment.bean.kernel.Kernel;
import org.anchoranalysis.mpp.segment.kernel.proposer.WeightedKernel;
import org.anchoranalysis.mpp.segment.kernel.proposer.WeightedKernelList;
import org.anchoranalysis.mpp.segment.transformer.StateTransformer;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;

@AllArgsConstructor
public class KernelUpdaterSimple<S, T, U> implements KernelUpdater<S, T, U> {

    private U marks;
    private WeightedKernelList<S, U> allKernels;
    private StateTransformer<Optional<T>, Optional<S>> transformer;

    @Override
    public void kernelAccepted(
            Kernel<S, U> kernel, Optional<T> current, T proposed, TransformationContext context)
            throws UpdateMarkSetException {
        try {
            Optional<S> currentTransformed = transform(current, context);

            S proposedTransformed =
                    transform(Optional.of(proposed), context)
                            .orElseThrow(
                                    () ->
                                            new UpdateMarkSetException(
                                                    "Transform returned empty, which is not allowed"));

            updateAfterAccept(kernel, currentTransformed, proposedTransformed);

            informAllKernelsAfterAccept(proposedTransformed);

        } catch (OperationFailedException e) {
            throw new UpdateMarkSetException(e);
        }
    }

    private Optional<S> transform(Optional<T> stateOptional, TransformationContext context)
            throws OperationFailedException {
        return OptionalUtilities.flatMap(
                stateOptional, state -> transformer.transform(stateOptional, context));
    }

    private void updateAfterAccept(Kernel<S, U> kernel, Optional<S> current, S proposed)
            throws UpdateMarkSetException {
        OptionalUtilities.ifPresent(
                current,
                currentInternal -> kernel.updateAfterAcceptance(marks, currentInternal, proposed));
    }

    private void informAllKernelsAfterAccept(S state) {
        // We inform ALL kernels of the new Energy
        for (WeightedKernel<S, U> weighted : allKernels) {
            // INFORM KERNEL
            weighted.getKernel().informLatestState(state);
        }
    }
}
