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
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.calc.NamedFeatureCalculationException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import lombok.Getter;
import lombok.Setter;

public class KernelExchange extends KernelIndependent<CfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer = null;
    // END BEAN PROPERTIES

    private Mark markExst;
    private Mark markNew;

    @Override
    public double calcAccptProb(
            int exstSize,
            int propSize,
            double poissonIntensity,
            ImageDimensions dimensions,
            double densityRatio) {
        return Math.min(1.0, densityRatio);
    }

    @Override
    public String dscrLast() {
        if (markNew != null) {
            return String.format("%s(%d)", getBeanName(), markExst.getId());
        } else {
            return getBeanName();
        }
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized exst,
            CfgNRGPixelized accptd)
            throws UpdateMarkSetException {

        VoxelizedMarkMemo memoNew = accptd.getMemoForMark(markNew);
        exst.exchangeOnUpdatablePairList(updatableMarkSetCollection, markExst, memoNew);
    }

    @Override
    public int[] changedMarkIDArray() {
        return new int[] {markExst.getId(), markNew.getId()};
    }

    @Override
    public Optional<CfgNRGPixelized> makeProposal(
            Optional<CfgNRGPixelized> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (!exst.isPresent()) {
            return Optional.empty();
        }

        ProposerContext propContext = context.proposer();

        // Pick an element from the existing configuration
        int index = exst.get().getCfg().randomIndex(propContext.getRandomNumberGenerator());

        markExst = exst.get().getCfg().get(index);

        // We copy the particular mark in question
        markNew = markExst.duplicate();

        VoxelizedMarkMemo pmmMarkNew = propContext.create(markNew);

        // Change the location of the mark
        boolean succ;
        try {
            succ = markProposer.propose(pmmMarkNew, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose a mark due to abnormal exception", e);
        }

        if (!succ) {
            return Optional.empty();
        }

        // We calculate a new NRG by exchanging our marks
        CfgNRGPixelized newNRG = exst.get().shallowCopy();
        try {
            newNRG.exchange(index, pmmMarkNew, propContext.getNrgStack());
        } catch (NamedFeatureCalculationException e) {
            throw new KernelCalcNRGException(String.format("Cannot exchange index %d", index), e);
        }

        return Optional.of(newNRG);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }
}
