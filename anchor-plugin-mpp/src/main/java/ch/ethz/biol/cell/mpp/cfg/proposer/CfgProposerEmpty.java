/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.proposer;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

public class CfgProposerEmpty extends CfgProposer {

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<Cfg> propose(CfgGen cfgGen, ProposerContext context)
            throws ProposalAbnormalFailureException {
        return Optional.of(new Cfg());
    }
}
