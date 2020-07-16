/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelInitialCfg extends KernelIndependent<Cfg> {

    // START BEAN LIST
    @BeanField private CfgProposer cfgProposer;
    // END BEAN LIST

    private Optional<Cfg> lastCfg;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return cfgProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<Cfg> makeProposal(Optional<Cfg> exst, KernelCalcContext context)
            throws KernelCalcNRGException {
        this.lastCfg = InitCfgUtilities.propose(cfgProposer, context);
        return lastCfg;
    }

    @Override
    public double calcAccptProb(
            int exstSize,
            int propSize,
            double poissonIntensity,
            ImageDimensions dimensions,
            double densityRatio) {
        // We always accept
        return 1;
    }

    @Override
    public String dscrLast() {
        return String.format("initialCfg(size=%d)", this.lastCfg.map(Cfg::size).orElse(-1));
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection, Cfg exst, Cfg accptd)
            throws UpdateMarkSetException {
        // NOTHING TO DO
    }

    @Override
    public int[] changedMarkIDArray() {
        return this.lastCfg.map(Cfg::createIdArr).orElse(new int[] {});
    }

    public CfgProposer getCfgProposer() {
        return cfgProposer;
    }

    public void setCfgProposer(CfgProposer cfgProposer) {
        this.cfgProposer = cfgProposer;
    }
}
