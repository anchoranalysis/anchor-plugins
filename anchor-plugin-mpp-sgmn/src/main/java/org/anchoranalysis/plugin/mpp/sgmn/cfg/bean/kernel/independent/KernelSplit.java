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
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoList;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.NamedFeatureCalculationException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelSplit extends KernelPosNeg<CfgNRGPixelized> {

    // START BEAN
    @BeanField @Getter @Setter private MarkSplitProposer splitProposer = null;

    @BeanField @Getter @Setter private MarkFromCfgProposer markFromCfgProposer = null;
    // END BEAN

    private Optional<Mark> markExst;
    private Optional<PairPxlMarkMemo> pairNew;

    @Override
    public Optional<CfgNRGPixelized> makeProposal(
            Optional<CfgNRGPixelized> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (!exst.isPresent()) {
            return Optional.empty();
        }

        ProposerContext propContext = context.proposer();

        try {
            this.markExst = markFromCfgProposer.markFromCfg(exst.get().getCfg(), propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Could not propose a mark due to an abnormal exception", e);
        }

        if (!markExst.isPresent()) {
            propContext.getErrorNode().add("cannot find an existing mark to split");
            pairNew = Optional.empty();
            markExst = Optional.empty();
            return Optional.empty();
        }

        int markExstIndex = exst.get().getCfg().indexOf(markExst.get());
        VoxelizedMarkMemo pmmMarkExst = exst.get().getMemoForIndex(markExstIndex);

        try {
            // Let's get positions for our two marks
            pairNew = splitProposer.propose(pmmMarkExst, propContext, context.cfgGen().getCfgGen());
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose a mark-split due to abnormal exception", e);
        }

        if (!pairNew.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(
                createCfgNRG(
                        exst.get(),
                        propContext.getNrgStack().getNrgStack(),
                        pairNew.get(),
                        markExstIndex));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return splitProposer.isCompatibleWith(testMark)
                && markFromCfgProposer.isCompatibleWith(testMark);
    }

    private static CfgNRGPixelized createCfgNRG(
            CfgNRGPixelized exst, NRGStack nrgStack, PairPxlMarkMemo pair, int markExstIndex)
            throws KernelCalcNRGException {

        // We calculate a new NRG by exchanging our marks
        CfgNRGPixelized newNRG = exst.shallowCopy();
        try {
            newNRG.rmv(markExstIndex, nrgStack);
        } catch (NamedFeatureCalculationException e1) {
            throw new KernelCalcNRGException(
                    String.format("Cannot remove index %d", markExstIndex), e1);
        }

        try {
            newNRG.add(pair.getSource(), nrgStack);
            newNRG.add(pair.getDestination(), nrgStack);
        } catch (NamedFeatureCalculationException e) {
            throw new KernelCalcNRGException("Cannot add source and destination", e);
        }

        return newNRG;
    }

    @Override
    public double calcAccptProb(
            int exstSize,
            int propSize,
            double poissonIntensity,
            ImageDimensions dimensions,
            double densityRatio) {
        return densityRatio;
    }

    @Override
    public String dscrLast() {
        if (markExst.isPresent() && pairNew.isPresent()) {
            return String.format(
                    "%s %d into %d into %d",
                    getBeanName(),
                    markExst.get().getId(),
                    pairNew.get().getSource().getMark().getId(),
                    pairNew.get().getDestination().getMark().getId());
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

        MemoList memoList = exst.createDuplicatePxlMarkMemoList();

        VoxelizedMarkMemo memoExst = exst.getMemoForMark(this.markExst.get()); // NOSONAR

        updatableMarkSetCollection.rmv(memoList, memoExst);
        memoList.remove(memoExst);

        VoxelizedMarkMemo memoAdded1 = pairNew.get().getSource();

        // Should always find one
        assert memoAdded1 != null;
        updatableMarkSetCollection.add(memoList, memoAdded1);

        memoList.add(memoAdded1);

        VoxelizedMarkMemo memoAdded2 = pairNew.get().getDestination();
        assert memoAdded2 != null;

        updatableMarkSetCollection.add(memoList, memoAdded2);
    }

    @Override
    public int[] changedMarkIDArray() {
        Stream<Optional<Mark>> stream =
                Stream.of(
                        markExst,
                        pairNew.map(pmm -> pmm.getSource().getMark()),
                        pairNew.map(pmm -> pmm.getDestination().getMark()));
        return stream.filter(Optional::isPresent).mapToInt(opt -> opt.get().getId()).toArray();
    }
}
