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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoList;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelBirthAndKill extends KernelPosNeg<CfgNRGPixelized> {

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
    public Optional<CfgNRGPixelized> makeProposal(
            Optional<CfgNRGPixelized> exst, KernelCalcContext context)
            throws KernelCalcNRGException {

        if (!exst.isPresent()) {
            return Optional.empty();
        }

        markNew = context.cfgGen().getCfgGen().newTemplateMark();

        ProposerContext propContext = context.proposer();

        VoxelizedMarkMemo memoNew = propContext.create(markNew);
        if (!proposeMark(memoNew, propContext)) {
            return Optional.empty();
        }

        try {
            toKill =
                    KernelBirthAndKillHelper.determineKillObjects(
                            memoNew, exst.get(), regionID, overlapRatioThreshold);
        } catch (OperationFailedException e) {
            throw new KernelCalcNRGException("Cannot add kill-objects", e);
        }

        // Now we want to do another birth, and we take this somewhere from the memos,
        //  of the killed objects that isn't covered by the birthMark
        Optional<VoxelizedMarkMemo> pmmAdditional =
                maybeMakeAdditionalBirth(memoNew.getMark(), context.cfgGen().getCfgGen(), context);

        return OptionalUtilities.map(
                pmmAdditional,
                pmm ->
                        KernelBirthAndKillHelper.calcUpdatedNRG(
                                exst.get(), memoNew, pmm, toKill, propContext));
    }

    private Optional<VoxelizedMarkMemo> maybeMakeAdditionalBirth(
            Mark markNew, CfgGen cfgGen, KernelCalcContext context) throws KernelCalcNRGException {
        if (markProposerAdditionalBirth != null) {
            VoxelizedMarkMemo pmmAdditional =
                    KernelBirthAndKillHelper.makeAdditionalBirth(
                            markProposerAdditionalBirth, markNew, toKill, cfgGen, context);
            if (pmmAdditional != null) {
                markNewAdditional = pmmAdditional.getMark();
            }
            return Optional.ofNullable(pmmAdditional);
        } else {
            return Optional.empty();
        }
    }

    private boolean proposeMark(VoxelizedMarkMemo memoNew, ProposerContext context)
            throws KernelCalcNRGException {
        try {
            return markProposer.propose(memoNew, context);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose a mark due to abnormal exception%n%s%n", e);
        }
    }

    @Override
    public double calcAccptProb(
            int exstSize,
            int propSize,
            double poissonIntensity,
            ImageDimensions dimensions,
            double densityRatio) {

        double num = getProbNeg() * dimensions.getVolume() * poissonIntensity;
        double dem = getProbPos() * propSize;

        assert num > 0;
        assert dem > 0;

        double d = (densityRatio * num) / dem;

        assert !Double.isNaN(d);

        return Math.min(d, 1.0);
    }

    @Override
    public String dscrLast() {
        return String.format("birthAndKill(%d)", markNew.getId());
    }

    @Override
    public void updateAfterAccpt(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized exst,
            CfgNRGPixelized accptd)
            throws UpdateMarkSetException {

        MemoList memoList = exst.createDuplicatePxlMarkMemoList();

        addNewMark(this.markNew, updatableMarkSetCollection, exst, accptd, memoList);

        removeMarks(toKill, updatableMarkSetCollection, memoList);

        if (markNewAdditional != null) {
            addAdditionalMark(markNewAdditional, updatableMarkSetCollection, memoList, accptd);
        }
    }

    private static void addNewMark(
            Mark mark,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            CfgNRGPixelized exst,
            CfgNRGPixelized accptd,
            MemoList memoList)
            throws UpdateMarkSetException {
        VoxelizedMarkMemo memo = accptd.getMemoForMark(mark);

        exst.addToUpdatablePairList(updatableMarkSetCollection, memo);
        memoList.add(memo);
    }

    private static void removeMarks(
            List<VoxelizedMarkMemo> marks,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            MemoList memoList)
            throws UpdateMarkSetException {
        for (VoxelizedMarkMemo memoRmv : marks) {
            updatableMarkSetCollection.rmv(memoList, memoRmv);
            memoList.remove(memoRmv);
            // TODO come up with a better system other than these memoLists
        }
    }

    private static void addAdditionalMark(
            Mark markAdditional,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            MemoList memoList,
            CfgNRGPixelized accptd)
            throws UpdateMarkSetException {
        VoxelizedMarkMemo memoNewAdditional = accptd.getMemoForMark(markAdditional);
        updatableMarkSetCollection.add(memoList, memoNewAdditional);
        memoList.add(memoNewAdditional);
    }

    @Override
    public int[] changedMarkIDArray() {
        return new int[] {this.markNew.getId()};
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }
}
