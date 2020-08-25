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

package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.mpp.bean.proposer.MarkFromCollectionProposer;
import org.anchoranalysis.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.feature.mark.MemoList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.pair.PairPxlMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;

public class KernelSplit extends KernelPosNeg<VoxelizedMarksWithEnergy> {

    // START BEAN
    @BeanField @Getter @Setter private MarkSplitProposer splitProposer = null;

    @BeanField @Getter @Setter private MarkFromCollectionProposer markFromMarksProposer = null;
    // END BEAN

    private Optional<Mark> markExst;
    private Optional<PairPxlMarkMemo> pairNew;

    @Override
    public Optional<VoxelizedMarksWithEnergy> makeProposal(
            Optional<VoxelizedMarksWithEnergy> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        ProposerContext propContext = context.proposer();

        try {
            this.markExst =
                    markFromMarksProposer.selectMarkFrom(
                            existing.get().getMarks().getMarks(), propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Could not propose a mark due to an abnormal exception", e);
        }

        if (!markExst.isPresent()) {
            propContext.getErrorNode().add("cannot find an existing mark to split");
            pairNew = Optional.empty();
            markExst = Optional.empty();
            return Optional.empty();
        }

        int markExstIndex = existing.get().indexOf(markExst.get());
        VoxelizedMarkMemo pmmMarkExst = existing.get().getMemoForIndex(markExstIndex);

        try {
            // Let's get positions for our two marks
            pairNew = splitProposer.propose(pmmMarkExst, propContext, context.getMarkFactory());
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose a mark-split due to abnormal exception", e);
        }

        if (!pairNew.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(
                createMarks(
                        existing.get(),
                        propContext.getEnergyStack().getEnergyStack(),
                        pairNew.get(),
                        markExstIndex));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return splitProposer.isCompatibleWith(testMark)
                && markFromMarksProposer.isCompatibleWith(testMark);
    }

    private static VoxelizedMarksWithEnergy createMarks(
            VoxelizedMarksWithEnergy existing,
            EnergyStackWithoutParams energyStack,
            PairPxlMarkMemo pair,
            int markExistingIndex)
            throws KernelCalculateEnergyException {

        // We calculate a new Energy by exchanging our marks
        VoxelizedMarksWithEnergy marksCopied = existing.shallowCopy();
        try {
            marksCopied.remove(markExistingIndex, energyStack);
        } catch (NamedFeatureCalculateException e1) {
            throw new KernelCalculateEnergyException(
                    String.format("Cannot remove index %d", markExistingIndex), e1);
        }

        try {
            marksCopied.add(pair.getSource(), energyStack);
            marksCopied.add(pair.getDestination(), energyStack);
        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException("Cannot add source and destination", e);
        }

        return marksCopied;
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {
        return densityRatio;
    }

    @Override
    public String describeLast() {
        if (markExst.isPresent() && pairNew.isPresent()) {
            return String.format(
                    "%s %d into %d into %d",
                    getBeanName(),
                    markExst.get().getId(), // NOSONAR
                    pairNew.get().getSource().getMark().getId(), // NOSONAR
                    pairNew.get().getDestination().getMark().getId()); // NOSONAR
        } else {
            return getBeanName();
        }
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            VoxelizedMarksWithEnergy exst,
            VoxelizedMarksWithEnergy accptd)
            throws UpdateMarkSetException {

        MemoList memoList = exst.createDuplicatePxlMarkMemoList();

        VoxelizedMarkMemo memoExst = exst.getMemoForMark(this.markExst.get()); // NOSONAR

        updatableMarkSetCollection.remove(memoList, memoExst);
        memoList.remove(memoExst);

        VoxelizedMarkMemo memoAdded1 = pairNew.get().getSource(); // NOSONAR

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
