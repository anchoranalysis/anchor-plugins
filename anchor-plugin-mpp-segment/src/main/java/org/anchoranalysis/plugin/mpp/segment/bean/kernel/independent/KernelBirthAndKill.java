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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.MemoList;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;

public class KernelBirthAndKill extends KernelPosNeg<VoxelizedMarksWithEnergy, UpdatableMarksList> {

    // START BEANS
    @BeanField @Getter @Setter private double overlapRatioThreshold = 0.1;

    @BeanField @Getter @Setter private MarkProposer markProposer = null;

    // Optional proposal for doing an additional birth
    @BeanField @OptionalBean @Getter @Setter
    private MarkProposer markProposerAdditionalBirth = null;

    @BeanField @Getter @Setter private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
    // END BEANS

    private Mark markNew;
    private Mark markNewAdditional;

    private List<VoxelizedMarkMemo> toKill;

    @Override
    public Optional<VoxelizedMarksWithEnergy> makeProposal(
            Optional<VoxelizedMarksWithEnergy> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        markNew = context.getMarkFactory().newTemplateMark();

        ProposerContext propContext = context.proposer();

        VoxelizedMarkMemo memoNew = propContext.create(markNew);
        if (!proposeMark(memoNew, propContext)) {
            return Optional.empty();
        }

        try {
            toKill =
                    KernelBirthAndKillHelper.determineKillObjects(
                            memoNew, existing.get(), regionID, overlapRatioThreshold);
        } catch (OperationFailedException e) {
            throw new KernelCalculateEnergyException("Cannot add kill-objects", e);
        }

        // Now we want to do another birth, and we take this somewhere from the memos,
        //  of the killed objects that isn't covered by the birthMark
        Optional<VoxelizedMarkMemo> pmmAdditional =
                maybeMakeAdditionalBirth(memoNew.getMark(), context.getMarkFactory(), context);

        return OptionalUtilities.map(
                pmmAdditional,
                pmm ->
                        KernelBirthAndKillHelper.updatedEnergy(
                                existing.get(), memoNew, pmm, toKill, propContext));
    }

    private Optional<VoxelizedMarkMemo> maybeMakeAdditionalBirth(
            Mark markNew, MarkWithIdentifierFactory markFactory, KernelCalculationContext context)
            throws KernelCalculateEnergyException {
        if (markProposerAdditionalBirth != null) {
            VoxelizedMarkMemo pmmAdditional =
                    KernelBirthAndKillHelper.makeAdditionalBirth(
                            markProposerAdditionalBirth, markNew, toKill, markFactory, context);
            if (pmmAdditional != null) {
                markNewAdditional = pmmAdditional.getMark();
            }
            return Optional.ofNullable(pmmAdditional);
        } else {
            return Optional.empty();
        }
    }

    private boolean proposeMark(VoxelizedMarkMemo memoNew, ProposerContext context)
            throws KernelCalculateEnergyException {
        try {
            return markProposer.propose(memoNew, context);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose a mark due to abnormal exception%n%s%n", e);
        }
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {

        double numerator = getProbNeg() * dimensions.calculateVolume() * poissonIntensity;
        double denominator = getProbPos() * proposalSize;

        assert numerator > 0;
        assert denominator > 0;

        double d = (densityRatio * numerator) / denominator;

        assert !Double.isNaN(d);

        return Math.min(d, 1.0);
    }

    @Override
    public String describeLast() {
        return String.format("birthAndKill(%d)", markNew.getIdentifier());
    }

    @Override
    public void updateAfterAcceptance(
            UpdatableMarksList updatableState,
            VoxelizedMarksWithEnergy exst,
            VoxelizedMarksWithEnergy accptd)
            throws UpdateMarkSetException {

        MemoList memoList = exst.createDuplicatePxlMarkMemoList();

        addNewMark(this.markNew, updatableState, exst, accptd, memoList);

        removeMarks(toKill, updatableState, memoList);

        if (markNewAdditional != null) {
            addAdditionalMark(markNewAdditional, updatableState, memoList, accptd);
        }
    }

    private static void addNewMark(
            Mark mark,
            UpdatableMarksList updatableMarkSetCollection,
            VoxelizedMarksWithEnergy exst,
            VoxelizedMarksWithEnergy accptd,
            MemoList memoList)
            throws UpdateMarkSetException {
        VoxelizedMarkMemo memo = accptd.getMemoForMark(mark);

        exst.addToUpdatablePairList(updatableMarkSetCollection, memo);
        memoList.add(memo);
    }

    private static void removeMarks(
            List<VoxelizedMarkMemo> marks, UpdatableMarksList updatableMarks, MemoList memoList)
            throws UpdateMarkSetException {
        for (VoxelizedMarkMemo memoRemove : marks) {
            updatableMarks.remove(memoList, memoRemove);
            memoList.remove(memoRemove);
        }
    }

    private static void addAdditionalMark(
            Mark markAdditional,
            UpdatableMarksList updatableMarkSetCollection,
            MemoList memoList,
            VoxelizedMarksWithEnergy accpted)
            throws UpdateMarkSetException {
        VoxelizedMarkMemo memoNewAdditional = accpted.getMemoForMark(markAdditional);
        updatableMarkSetCollection.add(memoList, memoNewAdditional);
        memoList.add(memoNewAdditional);
    }

    @Override
    public int[] changedMarkIDArray() {
        return new int[] {this.markNew.getIdentifier()};
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }
}
