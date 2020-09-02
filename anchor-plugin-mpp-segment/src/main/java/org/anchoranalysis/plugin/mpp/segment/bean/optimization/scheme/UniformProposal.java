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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.scheme;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.OptimizationScheme;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;
import org.anchoranalysis.mpp.segment.optimization.OptimizationContext;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackReceiver;

/**
 * Repeatedly propose new collections of marks and take the collection with optimal energy.
 * 
 * <p>Each proposal is independent of the previous state.
 * 
 * @author Owen Feehan
 *
 * @param <S>
 */
@NoArgsConstructor
public class UniformProposal<S> extends OptimizationScheme<S, S> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int iterations = -1;

    @BeanField @Getter @Setter private ExtractScoreSize<S> extractScoreSize;
    // END BEAN PROPERTIES

    // Finds an optimum by generating a certain number of configurations
    @Override
    public S findOptimum(
            KernelProposer<S> kernelProposer,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            FeedbackReceiver<S> feedbackReceiver,
            OptimizationContext initContext)
            throws OptimizationTerminatedEarlyException {

        S best = null;

        KernelCalculationContext context =
                initContext.calculateContext(initContext.markFactoryContext());

        try {
            kernelProposer.initBeforeCalc(context);

            for (int i = 0; i < iterations; i++) {
                Optional<S> proposal =
                        kernelProposer.getInitialKernel().makeProposal(Optional.empty(), context);

                if (proposal.isPresent() && isSuperior(proposal.get(), best)) {
                    best = proposal.get();
                }
            }
        } catch (KernelCalculateEnergyException | InitException e) {
            throw new OptimizationTerminatedEarlyException("Optimization terminated early", e);
        }

        return best;
    }
    
    private boolean isSuperior(S proposal, S best) {
        return (best == null || extractScore(proposal) > extractScore(best));
    }

    private double extractScore(S item) {
        return extractScoreSize.extractScore(item);
    }
}
