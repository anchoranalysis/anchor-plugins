/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;

public class OptSchemeUnifPerm<S> extends OptScheme<S, S> {

    // START BEAN PROPERTIES
    @BeanField private int numItr = -1;

    @BeanField private ExtractScoreSize<S> extractScoreSize;
    // END BEAN PROPERTIES

    public OptSchemeUnifPerm() {}

    public OptSchemeUnifPerm(int numItr) {
        super();
        this.numItr = numItr;
    }

    // Finds an optimum by generating a certain number of configurations
    @Override
    public S findOpt(
            KernelProposer<S> kernelProposer,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            FeedbackReceiver<S> feedbackReceiver,
            OptSchemeContext initContext)
            throws OptTerminatedEarlyException {

        S best = null;

        KernelCalcContext context = initContext.calcContext(initContext.cfgGenContext());

        try {
            kernelProposer.initBeforeCalc(context);

            for (int i = 0; i < this.numItr; i++) {
                Optional<S> cfgNRG =
                        kernelProposer.getInitialKernel().makeProposal(Optional.empty(), context);

                if (!cfgNRG.isPresent()) {
                    continue;
                }

                // We find the maximum
                if (best == null || extractScore(cfgNRG.get()) > extractScore(best)) {
                    best = cfgNRG.get();
                }
            }
        } catch (KernelCalcNRGException | InitException e) {
            throw new OptTerminatedEarlyException("Optimization terminated early", e);
        }

        return best;
    }

    private double extractScore(S item) {
        return extractScoreSize.extractScore(item);
    }

    public int getNumItr() {
        return numItr;
    }

    public void setNumItr(int numItr) {
        this.numItr = numItr;
    }

    public ExtractScoreSize<S> getExtractScoreSize() {
        return extractScoreSize;
    }

    public void setExtractScoreSize(ExtractScoreSize<S> extractScoreSize) {
        this.extractScoreSize = extractScoreSize;
    }
}
