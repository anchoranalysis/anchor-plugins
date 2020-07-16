/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.assigner.KernelAssignerCalcNRGFromKernel;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

/**
 * How assignments and other decisions are made in the SimulatedAnnealing optimizaton
 *
 * @author Owen Feehan
 * @param <S> reporting back type
 * @param <T> state-type for optimization
 * @param <U> target-type for kernel assignment
 */
public abstract class AssignMode<S, T, U> extends AnchorBean<AssignMode<S, T, U>> {

    public abstract AccptProbCalculator<T> probCalculator(AnnealScheme annealScheme);

    public KernelAssigner<U, T> kernelAssigner(TransformationContext tc) {
        return new KernelAssignerCalcNRGFromKernel<>(kernelStateBridge());
    }

    public abstract KernelStateBridge<U, T> kernelStateBridge();

    public abstract StateReporter<T, S> stateReporter();

    public abstract ExtractScoreSize<S> extractScoreSizeReport();

    public abstract ExtractScoreSize<T> extractScoreSizeState();
}
