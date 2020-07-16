/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelDeath;

public class KernelDeathPixelized extends KernelDeath<CfgNRGPixelized> {

    @Override
    protected Optional<MarkAnd<Mark, CfgNRGPixelized>> removeAndUpdateNrg(
            CfgNRGPixelized exst, ProposerContext context) throws KernelCalcNRGException {
        return removeMarkAndUpdateNrg(exst, context);
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized nrgExst,
            CfgNRGPixelized nrgNew)
            throws UpdateMarkSetException {
        OptionalUtilities.ifPresent(
                getMarkRmv(),
                mark -> nrgExst.rmvFromUpdatablePairList(updatableMarkSetCollection, mark));
    }

    private static Optional<MarkAnd<Mark, CfgNRGPixelized>> removeMarkAndUpdateNrg(
            CfgNRGPixelized exst, ProposerContext propContext) throws KernelCalcNRGException {

        int index = selectIndexToRmv(exst.getCfg(), propContext);

        if (index == -1) {
            return Optional.empty();
        }

        return Optional.of(
                new MarkAnd<>(
                        exst.getMemoForIndex(index).getMark(),
                        calcUpdatedNRGAfterRemoval(index, exst, propContext)));
    }

    private static CfgNRGPixelized calcUpdatedNRGAfterRemoval(
            int index, CfgNRGPixelized exst, ProposerContext propContext)
            throws KernelCalcNRGException {
        // We calculate a new NRG by exchanging our marks
        CfgNRGPixelized newNRG = exst.shallowCopy();

        try {
            newNRG.rmv(index, propContext.getNrgStack().getNrgStack());
        } catch (FeatureCalcException e) {
            throw new KernelCalcNRGException(String.format("Cannot remove index %d", index), e);
        }

        return newNRG;
    }
}
