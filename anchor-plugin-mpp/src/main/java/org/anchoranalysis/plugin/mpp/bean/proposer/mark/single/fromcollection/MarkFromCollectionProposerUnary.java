package org.anchoranalysis.plugin.mpp.bean.proposer.mark.single.fromcollection;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCollectionProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkCollection;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import lombok.Getter;
import lombok.Setter;

public abstract class MarkFromCollectionProposerUnary extends MarkFromCollectionProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkFromCollectionProposer proposer;
    // END BEAN PROPERTIES
    
    @Override
    public Optional<Mark> selectMarkFrom(MarkCollection marks, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return selectMarkFrom(marks, proposer, context);
    }
    
    protected abstract Optional<Mark> selectMarkFrom(MarkCollection marks, MarkFromCollectionProposer proposer, ProposerContext context)
            throws ProposalAbnormalFailureException;
    
    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return proposer.isCompatibleWith(testMark);
    }
}
