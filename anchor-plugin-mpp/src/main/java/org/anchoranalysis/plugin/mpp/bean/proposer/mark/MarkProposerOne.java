/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;

public abstract class MarkProposerOne extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField private MarkProposer proposer;
    // END BEAN PROPERTIES

    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return propose(inputMark, context, proposer);
    }

    protected abstract boolean propose(
            VoxelizedMarkMemo inputMark, ProposerContext context, MarkProposer source)
            throws ProposalAbnormalFailureException;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return proposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return proposer.proposalVisualization(detailed);
    }

    public MarkProposer getProposer() {
        return proposer;
    }

    public void setProposer(MarkProposer proposer) {
        this.proposer = proposer;
    }
}
