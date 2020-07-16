/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;

public class Position extends MarkProposer {

    // START BEAN PROPERTIES
    @BeanField private PositionProposerBean positionProposer;
    // END BEAN PROPERTIES

    @Override
    public boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context) {

        // Assume this is a MarkPos
        MarkAbstractPosition inputMarkPos = (MarkAbstractPosition) inputMark.getMark();
        inputMark.reset();

        // We create a new point, so that if it's changed later it doesn't affect the original home
        Optional<Point3d> pos =
                positionProposer.propose(context.addErrorLevel("MarkPositionProposer"));

        if (!pos.isPresent()) {
            context.getErrorNode().add("positionProposer returned null");
            return false;
        }

        inputMarkPos.setPos(pos.get());

        return true;
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
        return Optional.empty();
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return testMark instanceof MarkAbstractPosition;
    }

    public PositionProposerBean getPositionProposer() {
        return positionProposer;
    }

    public void setPositionProposer(PositionProposerBean positionProposer) {
        this.positionProposer = positionProposer;
    }
}
