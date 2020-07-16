/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgNRGPixelizedFactory;

public class KernelInitialCfgNRG extends KernelIndependent<CfgNRGPixelized> {

    // START BEAN LIST
    @BeanField private CfgProposer cfgProposer;
    // END BEAN LIST

    private Optional<Cfg> lastCfg;

    public KernelInitialCfgNRG() {
        // Standard bean constuctor
    }

    public KernelInitialCfgNRG(CfgProposer cfgProposer) {
        this.cfgProposer = cfgProposer;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return cfgProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<CfgNRGPixelized> makeProposal(
            Optional<CfgNRGPixelized> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        ProposerContext propContext = context.proposer();

        // We don't expect an existing exsting CfgNRG, but rather null (or whatever)

        // Initial cfg
        Optional<Cfg> cfg = proposeCfg(context.cfgGen().getCfgGen(), propContext);

        this.lastCfg = cfg;

        try {
            return OptionalUtilities.map(
                    cfg, c -> CfgNRGPixelizedFactory.createFromCfg(c, context, getLogger()));
        } catch (CreateException e) {
            throw new KernelCalcNRGException("Cannot create CfgNRGPixelized", e);
        }
    }

    private Optional<Cfg> proposeCfg(CfgGen cfgGen, ProposerContext propContext)
            throws KernelCalcNRGException {
        try {
            return cfgProposer.propose(cfgGen, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose an initial-cfg due to an abnormal error", e);
        }
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
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized exst,
            CfgNRGPixelized accptd)
            throws UpdateMarkSetException {
        accptd.addAllToUpdatablePairList(updatableMarkSetCollection);
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
