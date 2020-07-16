/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;

public class OrderedList extends MarkProposerFromList {

    @Override
    protected boolean propose(
            VoxelizedMarkMemo inputMark,
            ProposerContext context,
            List<MarkProposer> markProposerList)
            throws ProposalAbnormalFailureException {
        for (MarkProposer markProposer : markProposerList) {
            // Calculate position
            if (!markProposer.propose(inputMark, context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Optional<CreateProposalVisualization> proposalVisualization(
            boolean detailed, List<MarkProposer> markProposerList) {
        return Optional.empty();
    }
}
