/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.proposer.split;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;

public class MarkSplitProposerReference extends MarkSplitProposer {

    // Start BEAN
    @BeanField private String id;
    // End BEAN

    private MarkSplitProposer delegate = null;

    @Override
    public void onInit(MPPInitParams pso) throws InitException {
        super.onInit(pso);
        try {
            delegate = getInitializationParameters().getMarkSplitProposerSet().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return delegate.isCompatibleWith(testMark);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Optional<PairPxlMarkMemo> propose(
            VoxelizedMarkMemo mark, ProposerContext context, CfgGen cfgGen)
            throws ProposalAbnormalFailureException {
        return delegate.propose(mark, context, cfgGen);
    }
}
