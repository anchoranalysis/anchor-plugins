/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InitCfgUtilities {

    public static Optional<Cfg> propose(CfgProposer cfgProposer, KernelCalcContext context)
            throws KernelCalcNRGException {
        ProposerContext propContext = context.proposer();

        // We don't expect an existing exsting CfgNRG, but rather null (or whatever)

        // Initial cfg
        return proposeCfg(context.cfgGen().getCfgGen(), cfgProposer, propContext);
    }

    private static Optional<Cfg> proposeCfg(
            CfgGen cfgGen, CfgProposer cfgProposer, ProposerContext propContext)
            throws KernelCalcNRGException {
        try {
            return cfgProposer.propose(cfgGen, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose an initial-cfg due to an abnormal error", e);
        }
    }
}
