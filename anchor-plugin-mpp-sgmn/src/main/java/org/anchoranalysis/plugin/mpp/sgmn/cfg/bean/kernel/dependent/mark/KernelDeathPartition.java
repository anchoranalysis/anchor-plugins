/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.dependent.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelDeath;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

public class KernelDeathPartition extends KernelDeath<CfgFromPartition> {

    @Override
    protected Optional<MarkAnd<Mark, CfgFromPartition>> removeAndUpdateNrg(
            CfgFromPartition exst, ProposerContext context) throws KernelCalcNRGException {

        int index = selectIndexToRmv(exst.getCfg(), context);

        if (index == -1) {
            return Optional.empty();
        }

        Cfg cfgNew = exst.getCfg().shallowCopy();
        cfgNew.remove(index);
        return Optional.of(new MarkAnd<>(exst.getCfg().get(index), exst.copyChange(cfgNew)));
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgFromPartition nrgExst,
            CfgFromPartition nrgNew)
            throws UpdateMarkSetException {
        OptionalUtilities.ifPresent(getMarkRmv(), nrgNew.getPartition()::moveAcceptedToAvailable);
    }
}
