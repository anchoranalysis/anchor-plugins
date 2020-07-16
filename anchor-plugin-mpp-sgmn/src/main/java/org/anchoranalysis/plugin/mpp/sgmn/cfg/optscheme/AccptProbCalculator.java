/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import java.util.Optional;
import java.util.function.ToDoubleFunction;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;

@RequiredArgsConstructor
public class AccptProbCalculator<T> {

    // START REQUIRED ARGUMENTS
    private final AnnealScheme annealScheme;
    private final ExtractScoreSize<T> extracter;
    // END REQUIRED ARGUMENTS

    public double calcAccptProb(
            Kernel<?> kernel, Optional<T> crnt, T proposal, int iter, KernelCalcContext context) {
        return kernel.calcAccptProb(
                sizeOrZero(crnt),
                sizeOrZero(Optional.of(proposal)),
                context.cfgGen().getCfgGen().getReferencePoissonIntensity(),
                context.proposer().getDimensions(),
                calcDensityRatio(crnt, Optional.of(proposal), iter));
    }

    public ToDoubleFunction<T> getFuncScore() {
        return extracter::extractScore;
    }

    private int sizeOrZero(Optional<T> crnt) {
        return crnt.map(extracter::extractSize).orElse(0);
    }

    private double calcDensityRatio(Optional<T> crnt, Optional<T> proposal, int iter) {

        if (!proposal.isPresent() || !crnt.isPresent()) {
            return Double.NaN;
        }

        return annealScheme.calcDensityRatio(
                extracter.extractScore(proposal.get()), extracter.extractScore(crnt.get()), iter);
    }
}
