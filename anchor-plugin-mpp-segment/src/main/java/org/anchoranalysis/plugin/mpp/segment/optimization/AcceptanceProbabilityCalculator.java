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

package org.anchoranalysis.plugin.mpp.segment.optimization;

import java.util.Optional;
import java.util.function.ToDoubleFunction;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.mpp.bean.AnnealScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.Kernel;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculationContext;

@RequiredArgsConstructor
public class AcceptanceProbabilityCalculator<T> {

    // START REQUIRED ARGUMENTS
    private final AnnealScheme annealScheme;
    private final ExtractScoreSize<T> extracter;
    // END REQUIRED ARGUMENTS

    public double calculateAcceptanceProbability(
            Kernel<?, ?> kernel,
            Optional<T> current,
            T proposal,
            int iteration,
            KernelCalculationContext context) {
        return kernel.calculateAcceptanceProbability(
                sizeOrZero(current),
                sizeOrZero(Optional.of(proposal)),
                context.getMarkFactory().getReferencePoissonIntensity(),
                context.proposer().dimensions(),
                densityRatio(current, Optional.of(proposal), iteration));
    }

    public ToDoubleFunction<T> getScoreExtracter() {
        return extracter::extractScore;
    }

    private int sizeOrZero(Optional<T> current) {
        return current.map(extracter::extractSize).orElse(0);
    }

    private double densityRatio(Optional<T> current, Optional<T> proposal, int iteration) {
        if (proposal.isPresent() && current.isPresent()) {
            return annealScheme.calculateDensityRatio(
                    extracter.extractScore(proposal.get()),
                    extracter.extractScore(current.get()),
                    iteration);
        } else {
            return Double.NaN;
        }
    }
}
