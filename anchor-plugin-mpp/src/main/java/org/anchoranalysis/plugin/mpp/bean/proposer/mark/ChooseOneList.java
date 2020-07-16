/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;

// Chooses one from a list
public class ChooseOneList extends MarkProposerFromList {

    private int lastIndex = -1;

    @Override
    protected boolean propose(
            VoxelizedMarkMemo inputMark,
            ProposerContext context,
            List<MarkProposer> markProposerList)
            throws ProposalAbnormalFailureException {

        int index = context.getRandomNumberGenerator().sampleIntFromRange(markProposerList.size());

        MarkProposer markProposer = markProposerList.get(index);

        lastIndex = index;

        // Calculate position
        return markProposer.propose(inputMark, context);
    }

    @Override
    protected Optional<CreateProposalVisualization> proposalVisualization(
            boolean detailed, List<MarkProposer> markProposerList) {
        if (lastIndex != -1) {
            return markProposerList.get(lastIndex).proposalVisualization(detailed);
        }
        return Optional.empty();
    }
}
