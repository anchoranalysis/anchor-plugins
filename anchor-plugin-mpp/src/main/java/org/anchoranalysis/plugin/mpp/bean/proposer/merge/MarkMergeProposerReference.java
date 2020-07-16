/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.merge;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

public class MarkMergeProposerReference extends MarkMergeProposer {

    // Start BEAN
    @BeanField @Getter @Setter private String id;
    // End BEAN

    private MarkMergeProposer delegate = null;

    @Override
    public void onInit(MPPInitParams pso) throws InitException {
        super.onInit(pso);
        try {
            delegate = getInitializationParameters().getMarkMergeProposerSet().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(
                    String.format(
                            "Cannot find referenced markSplitProposer '%s': %s",
                            id, e.summarize().toString()));
        }
    }

    @Override
    public Optional<Mark> propose(
            VoxelizedMarkMemo mark1, VoxelizedMarkMemo mark2, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return delegate.propose(mark1, mark2, context);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return delegate.isCompatibleWith(testMark);
    }
}
