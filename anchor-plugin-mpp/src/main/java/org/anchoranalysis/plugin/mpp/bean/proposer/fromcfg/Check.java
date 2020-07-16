/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.fromcfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

public class Check extends MarkFromCfgProposer {

    // START BEANS
    @BeanField private MarkFromCfgProposer markFromCfgProposer;

    @BeanField private CheckMark checkMark;
    // END BEANS

    @Override
    public Optional<Mark> markFromCfg(Cfg cfg, ProposerContext context)
            throws ProposalAbnormalFailureException {

        Optional<Mark> mark = markFromCfgProposer.markFromCfg(cfg, context);

        if (!mark.isPresent()) {
            return mark;
        }

        try {
            checkMark.start(context.getNrgStack());
        } catch (OperationFailedException e) {
            // Serious error
            context.getErrorNode().add(e);
            return Optional.empty();
        }

        try {
            if (checkMark.check(mark.get(), context.getRegionMap(), context.getNrgStack())) {

                checkMark.end();

                // We have a candidate mark
                return mark;
            } else {
                checkMark.end();
                return Optional.empty();
            }
        } catch (CheckException e) {
            checkMark.end();
            throw new ProposalAbnormalFailureException(
                    "Failed to check-mark due to abnormal exception", e);
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return checkMark.isCompatibleWith(testMark)
                && markFromCfgProposer.isCompatibleWith(testMark);
    }

    public CheckMark getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(CheckMark checkMark) {
        this.checkMark = checkMark;
    }

    public MarkFromCfgProposer getMarkFromCfgProposer() {
        return markFromCfgProposer;
    }

    public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
        this.markFromCfgProposer = markFromCfgProposer;
    }
}
