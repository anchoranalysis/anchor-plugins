/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridgeIdentity;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter.StateReporterIdentity;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

/**
 * Directly assigns a CfgNRG for every optimisation step
 *
 * @param S state-type for optimization
 */
public class DirectAssignMode<S> extends AssignMode<S, S, S> {

    // START BEAN PROPERTIES
    @BeanField private ExtractScoreSize<S> extractScoreSize;
    // END BEAN PROPERTIES

    public DirectAssignMode() {
        // Standard bean constructor
    }

    public DirectAssignMode(ExtractScoreSize<S> extractScoreSize) {
        this.extractScoreSize = extractScoreSize;
    }

    public AccptProbCalculator<S> probCalculator(AnnealScheme annealScheme) {
        return new AccptProbCalculator<>(annealScheme, extractScoreSize);
    }

    @Override
    public KernelStateBridge<S, S> kernelStateBridge() {
        return new KernelStateBridgeIdentity<>();
    }

    @Override
    public StateReporter<S, S> stateReporter() {
        return new StateReporterIdentity<>();
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeReport() {
        return extractScoreSize;
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeState() {
        return extractScoreSizeReport();
    }

    public ExtractScoreSize<S> getExtractScoreSize() {
        return extractScoreSize;
    }

    public void setExtractScoreSize(ExtractScoreSize<S> extractScoreSize) {
        this.extractScoreSize = extractScoreSize;
    }
}
