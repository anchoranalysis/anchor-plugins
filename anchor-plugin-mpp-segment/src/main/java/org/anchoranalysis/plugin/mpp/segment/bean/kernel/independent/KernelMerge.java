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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.feature.mark.MemoList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.mark.voxelized.memo.PxlMarkMemoFactory;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.pair.IdentifiablePair;
import org.anchoranalysis.mpp.pair.RandomCollection;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;

public class KernelMerge extends KernelPosNeg<VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkMergeProposer mergeMarkProposer;

    @BeanField @Getter @Setter private String simplePairCollectionID;
    // END BEAN PROPERTIES

    private IdentifiablePair<Mark> pair;
    private Optional<Mark> markAdded;

    @Getter @Setter private RandomCollection<IdentifiablePair<Mark>> pairCollection;

    @Override
    public void onInit(MPPInitParams pso) throws InitException {
        super.onInit(pso);

        try {
            pairCollection =
                    getInitializationParameters()
                            .getSimplePairCollection()
                            .getException(simplePairCollectionID);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }

        if (pairCollection == null) {
            throw new InitException(
                    String.format("pairCollection '%s' not found", simplePairCollectionID));
        }
    }

    @Override
    public Optional<VoxelizedMarksWithEnergy> makeProposal(
            Optional<VoxelizedMarksWithEnergy> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        ProposerContext propContext = context.proposer();

        pair = pairCollection.sampleRandomPairNonUniform(propContext.getRandomNumberGenerator());
        if (pair == null) {
            markAdded = Optional.empty();
            propContext.getErrorNode().add("cannot generate pair");
            return Optional.empty();
        }

        VoxelizedMarkMemo pmmSrc = existing.get().getMemoForMark(pair.getSource());
        VoxelizedMarkMemo pmmDest = existing.get().getMemoForMark(pair.getDestination());

        // How and why does this happen?
        if (pmmSrc == null || pmmDest == null) {
            pair = null;
            markAdded = Optional.empty();
            return Optional.empty();
        }

        try {
            markAdded = mergeMarkProposer.propose(pmmSrc, pmmDest, context.proposer());
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose a mark for merging due to abnormal exception", e);
        }

        // If we can't generate a successful merge, we cancel the kernel
        if (!markAdded.isPresent()) {
            pair = null;
            return Optional.empty();
        }

        markAdded.get().setId(context.getMarkFactory().idAndIncrement());

        return Optional.of(
                createMarks(
                        markAdded.get(),
                        existing.get(),
                        propContext.getEnergyStack(),
                        propContext.getRegionMap()));
    }

    private VoxelizedMarksWithEnergy createMarks(
            Mark mark,
            VoxelizedMarksWithEnergy existing,
            EnergyStack energyStack,
            RegionMap regionMap)
            throws KernelCalculateEnergyException {

        // we need to get indexes for each mark (well make this tidier)
        int sourceIndex = existing.indexOf(pair.getSource());
        int destinationIndex = existing.indexOf(pair.getDestination());

        if (sourceIndex == -1) {
            throw new KernelCalculateEnergyException("Cannot find source mark in pair");
        }
        if (destinationIndex == -1) {
            throw new KernelCalculateEnergyException("Cannot find destination mark in pair");
        }

        // We calculate a new Energy by exchanging our marks
        VoxelizedMarksWithEnergy voxelizedMarks = existing.shallowCopy();

        try {
            voxelizedMarks.removeTwo(sourceIndex, destinationIndex, energyStack.withoutParams());
        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException(
                    String.format("Cannot remove indexes %d and %d", sourceIndex, destinationIndex),
                    e);
        }

        VoxelizedMarkMemo pmm =
                PxlMarkMemoFactory.create(mark, energyStack.withoutParams(), regionMap);

        try {
            voxelizedMarks.add(pmm, energyStack.withoutParams());
        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException("Cannot add pmm", e);
        }

        return voxelizedMarks;
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
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            VoxelizedMarksWithEnergy existing,
            VoxelizedMarksWithEnergy accpted)
            throws UpdateMarkSetException {

        MemoList memoList = existing.createDuplicatePxlMarkMemoList();

        int rmvIndex1 = existing.indexOf(pair.getSource());
        int rmvIndex2 = existing.indexOf(pair.getDestination());

        VoxelizedMarkMemo memoSource = existing.getMemoForMark(pair.getSource());
        VoxelizedMarkMemo memoDest = existing.getMemoForMark(pair.getDestination());

        // We need to delete in the correct order
        if (rmvIndex2 > rmvIndex1) {
            updatableMarkSetCollection.remove(memoList, memoDest);
            memoList.remove(rmvIndex2);

            updatableMarkSetCollection.remove(memoList, memoSource);
            memoList.remove(rmvIndex1);
        } else {
            updatableMarkSetCollection.remove(memoList, memoSource);
            memoList.remove(rmvIndex1);

            updatableMarkSetCollection.remove(memoList, memoDest);
            memoList.remove(rmvIndex2);
        }

        VoxelizedMarkMemo memoAdded = accpted.getMemoForMark(markAdded.get()); // NOSONAR

        // Should always find one
        assert memoAdded != null;

        updatableMarkSetCollection.add(memoList, memoAdded);
    }

    @Override
    public String describeLast() {
        if (pair != null && pair.getSource() != null && pair.getDestination() != null) {
            return String.format(
                    "merge %d and %d into %d",
                    pair.getSource().getIdentifier(),
                    pair.getDestination().getIdentifier(),
                    markAdded.map(Mark::getIdentifier).orElse(-1));
        } else {
            return "merge";
        }
    }

    @Override
    public int[] changedMarkIDArray() {
        return new int[] {
            pair.getSource().getIdentifier(),
            pair.getDestination().getIdentifier(),
            markAdded.map(Mark::getIdentifier).orElse(-1)
        };
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return mergeMarkProposer.isCompatibleWith(testMark);
    }
}
