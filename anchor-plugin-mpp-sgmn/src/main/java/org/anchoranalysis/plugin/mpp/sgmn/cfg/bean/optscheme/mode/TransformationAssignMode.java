/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

/**
 * Applies a transformation to the kernel-type U to calculate the NRG used as the primary readout
 * during optimization
 *
 * <p>However, the kernel manipulation layer will always function in terms of the untransformed NRG
 * (U) as the optimization continues
 *
 * <p>The final transformation, as well as what's "reported" out use the TRANSFORMED (S) version
 *
 * @author Owen Feehan
 */
public class TransformationAssignMode<S, T, U> extends AssignMode<S, T, U> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KernelStateBridge<U, T> kernelStateBridge;

    @BeanField @Getter @Setter private StateReporter<T, S> stateReporter;

    @BeanField @Getter @Setter private ExtractScoreSize<S> extractScoreSizeReport;

    @BeanField @Getter @Setter private ExtractScoreSize<T> extractScoreSizeState;
    // END BEAN PROPERTIES

    @Override
    public AccptProbCalculator<T> probCalculator(AnnealScheme annealScheme) {
        return new AccptProbCalculator<>(annealScheme, extractScoreSizeState);
    }

    @Override
    public KernelStateBridge<U, T> kernelStateBridge() {
        return kernelStateBridge;
    }

    @Override
    public StateReporter<T, S> stateReporter() {
        return stateReporter;
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeReport() {
        return extractScoreSizeReport;
    }

    @Override
    public ExtractScoreSize<T> extractScoreSizeState() {
        return extractScoreSizeState;
    }
}
