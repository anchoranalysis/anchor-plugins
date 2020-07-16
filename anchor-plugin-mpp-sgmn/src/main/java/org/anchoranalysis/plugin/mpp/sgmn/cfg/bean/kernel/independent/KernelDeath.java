/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public abstract class KernelDeath<T> extends KernelPosNeg<T> {

    private Optional<Mark> markRmv = Optional.empty();

    @Override
    public Optional<T> makeProposal(Optional<T> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (!exst.isPresent()) {
            return Optional.empty();
        }

        Optional<MarkAnd<Mark, T>> markNrg = removeAndUpdateNrg(exst.get(), context.proposer());
        markRmv = markNrg.map(MarkAnd::getMark);
        return markNrg.map(MarkAnd::getCfgNrg);
    }

    protected abstract Optional<MarkAnd<Mark, T>> removeAndUpdateNrg(
            T exst, ProposerContext context) throws KernelCalcNRGException;

    @Override
    public double calcAccptProb(
            int exstSize,
            int propSize,
            double poissonIntens,
            ImageDimensions dimensions,
            double densityRatio) {

        if (exstSize <= 1) {
            return Math.min(1.0, densityRatio);
        }

        // Birth prob
        double num = getProbNeg() * exstSize;

        // Death prob
        double dem = getProbPos() * dimensions.getVolume() * poissonIntens;

        return Math.min(1.0, densityRatio * num / dem);
    }

    @Override
    public String dscrLast() {
        if (markRmv.isPresent()) {
            return String.format("death(%d)", markRmv.get().getId());
        } else {
            return "death";
        }
    }

    @Override
    public int[] changedMarkIDArray() {
        if (markRmv.isPresent()) {
            return new int[] {this.markRmv.get().getId()};
        } else {
            return new int[] {};
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    protected Optional<Mark> getMarkRmv() {
        return markRmv;
    }

    protected static int selectIndexToRmv(Cfg exst, ProposerContext propContext) {

        if (exst.size() == 0) {
            propContext.getErrorNode().add("configuration size is 0");
            return -1;
        }

        // Random mark
        return exst.randomIndex(propContext.getRandomNumberGenerator());
    }

    protected static class MarkAnd<S, T> {

        private S mark;
        private T cfgNrg;

        public MarkAnd(S mark, T cfgNrg) {
            super();
            this.mark = mark;
            this.cfgNrg = cfgNrg;
        }

        public S getMark() {
            return mark;
        }

        public T getCfgNrg() {
            return cfgNrg;
        }
    }
}
