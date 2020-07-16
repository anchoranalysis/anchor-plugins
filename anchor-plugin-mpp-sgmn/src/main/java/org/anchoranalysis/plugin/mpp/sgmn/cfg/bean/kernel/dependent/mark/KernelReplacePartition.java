/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.dependent.mark;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelReplace;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

public class KernelReplacePartition extends KernelReplace<CfgFromPartition> {

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    @Override
    public Optional<CfgFromPartition> makeProposal(
            Optional<CfgFromPartition> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (!exst.isPresent()) {
            return Optional.empty();
        }

        if (!hasBeenInit()) {
            init(createBirthKernel(getBirthRepeats()), new KernelDeathPartition());
        }

        return super.makeProposal(exst, context);
    }

    private static KernelBirthPartition createBirthKernel(int repeats) {
        KernelBirthPartition part = new KernelBirthPartition();
        part.setRepeats(repeats);
        return part;
    }
}
