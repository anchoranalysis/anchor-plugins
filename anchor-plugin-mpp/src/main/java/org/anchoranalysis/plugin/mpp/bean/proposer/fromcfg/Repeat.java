/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.fromcfg;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;

public class Repeat extends MarkFromCfgProposer {

    // START BEAN
    @BeanField private MarkFromCfgProposer markFromCfgProposer;

    @BeanField private int maxIter = 20;
    // END BEAN

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markFromCfgProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<Mark> markFromCfg(Cfg cfg, ProposerContext context)
            throws ProposalAbnormalFailureException {

        context.addErrorLevel("MarkFromCfgProposerRepeat");

        for (int i = 0; i < maxIter; i++) {
            Optional<Mark> mark = markFromCfgProposer.markFromCfg(cfg, context);
            if (mark.isPresent()) {
                return mark;
            }
        }

        context.getErrorNode().add("maxIter reached");

        return Optional.empty();
    }

    public MarkFromCfgProposer getMarkFromCfgProposer() {
        return markFromCfgProposer;
    }

    public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
        this.markFromCfgProposer = markFromCfgProposer;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
}
