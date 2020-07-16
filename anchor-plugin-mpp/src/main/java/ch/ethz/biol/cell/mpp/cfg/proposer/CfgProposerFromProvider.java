/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.proposer;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

public class CfgProposerFromProvider extends CfgProposer {

    // START BEAN FIELDS
    @BeanField private CfgProvider cfgProvider;
    // END BEAN FIELDS

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {
        try {
            return Optional.of(cfgProvider.create());
        } catch (CreateException e) {
            throw new ProposalAbnormalFailureException(e);
        }
    }

    public CfgProvider getCfgProvider() {
        return cfgProvider;
    }

    public void setCfgProvider(CfgProvider cfgProvider) {
        this.cfgProvider = cfgProvider;
    }
}
