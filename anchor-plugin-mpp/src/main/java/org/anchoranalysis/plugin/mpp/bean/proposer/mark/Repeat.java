/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;

// Repeat multiple times until we get a successful proposal
//  abandoning after maxIter is reached
public class Repeat extends MarkProposerOne {

    // START BEAN PROPERTIES
    @BeanField private int maxIter = 20;
    // END BEAN PROPERTIES

    @Override
    protected boolean propose(
            VoxelizedMarkMemo inputMark, ProposerContext context, MarkProposer source)
            throws ProposalAbnormalFailureException {

        for (int i = 0; i < maxIter; i++) {

            if (source.propose(inputMark, context)) {
                return true;
            }
        }

        context.getErrorNode().add("max number of iterations reached");
        return false;
    }

    public int getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }
}
