/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.dependent.mark;

import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelBirth;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

/**
 * Proposes new marks ONLY if they haven't already been proposed and accepted.
 *
 * <p>As an example, this is like sampling WITHOUT replacement.
 *
 * @author Owen Feehan
 */
public class KernelBirthPartition extends KernelBirth<CfgFromPartition> {

    @Override
    protected Optional<Set<Mark>> proposeNewMarks(
            CfgFromPartition exst, int number, KernelCalcContext context) {
        assert (exst != null);
        assert (exst.getPartition() != null);
        return exst.getPartition().sampleFromAvailable(number);
    }

    @Override
    protected Optional<CfgFromPartition> calcForNewMark(
            CfgFromPartition exst, Set<Mark> listMarksNew, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (listMarksNew == null || listMarksNew.isEmpty()) {
            return Optional.empty();
        }

        Cfg cfg = exst.getCfg();
        for (Mark m : listMarksNew) {
            cfg = calcUpdatedCfg(cfg, m);
        }

        return Optional.of(exst.copyChange(cfg));
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgFromPartition nrgExst,
            CfgFromPartition nrgNew)
            throws UpdateMarkSetException {
        if (getMarkNew().isPresent()) {
            nrgNew.getPartition().moveAvailableToAccepted(getMarkNew().get());
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    private static Cfg calcUpdatedCfg(Cfg exst, Mark mark) {
        Cfg newCfg = exst.shallowCopy();
        newCfg.add(mark);
        return newCfg;
    }
}
