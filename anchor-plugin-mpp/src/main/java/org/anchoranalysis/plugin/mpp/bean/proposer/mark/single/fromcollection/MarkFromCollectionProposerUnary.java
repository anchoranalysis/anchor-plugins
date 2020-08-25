package org.anchoranalysis.plugin.mpp.bean.proposer.mark.single.fromcollection;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.bean.proposer.MarkFromCollectionProposer;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;

public abstract class MarkFromCollectionProposerUnary extends MarkFromCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkFromCollectionProposer proposer;
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> selectMarkFrom(MarkCollection marks, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return selectMarkFrom(marks, proposer, context);
    }

    protected abstract Optional<Mark> selectMarkFrom(
            MarkCollection marks, MarkFromCollectionProposer proposer, ProposerContext context)
            throws ProposalAbnormalFailureException;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return proposer.isCompatibleWith(testMark);
    }
}
