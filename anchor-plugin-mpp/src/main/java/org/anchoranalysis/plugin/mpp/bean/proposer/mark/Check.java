/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.awt.Color;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.OperationFailedException;

public class Check extends MarkProposerOne {

    // BEAN PARAMETERS
    @BeanField private CheckMark checkMark = null;
    // END BEAN

    private Mark lastFailedMark;

    @Override
    protected boolean propose(
            VoxelizedMarkMemo inputMark, ProposerContext context, MarkProposer source)
            throws ProposalAbnormalFailureException {

        if (!source.propose(inputMark, context)) {
            lastFailedMark = null;
            return false;
        }

        try {
            checkMark.start(context.getNrgStack());
        } catch (OperationFailedException e) {
            context.getErrorNode().add(e);
            return false;
        }

        try {
            if (!checkMark.check(
                    inputMark.getMark(), context.getRegionMap(), context.getNrgStack())) {
                lastFailedMark = inputMark.getMark();
                return false;
            }
        } catch (CheckException e) {
            checkMark.end();
            throw new ProposalAbnormalFailureException(
                    "Failed to check-mark due to abnormal exception", e);
        }

        lastFailedMark = null;

        checkMark.end();

        return true;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        if (lastFailedMark != null) {
            return Optional.of(cfg -> cfg.addChangeID(lastFailedMark, new RGBColor(Color.ORANGE)));
        } else {
            return super.proposalVisualization(detailed);
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return super.isCompatibleWith(testMark) && checkMark.isCompatibleWith(testMark);
    }

    public CheckMark getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(CheckMark checkMark) {
        this.checkMark = checkMark;
    }
}
