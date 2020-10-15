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
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;

public class KernelExchange extends KernelIndependent<VoxelizedMarksWithEnergy> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProposer markProposer = null;
    // END BEAN PROPERTIES

    private Mark markExst;
    private Mark markNew;

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {
        return Math.min(1.0, densityRatio);
    }

    @Override
    public String describeLast() {
        if (markNew != null) {
            return String.format("%s(%d)", getBeanName(), markExst.getId());
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

        VoxelizedMarkMemo memoNew = accptd.getMemoForMark(markNew);
        exst.exchangeOnUpdatablePairList(updatableMarkSetCollection, markExst, memoNew);
    }

    @Override
    public int[] changedMarkIDArray() {
        return new int[] {markExst.getId(), markNew.getId()};
    }

    @Override
    public Optional<VoxelizedMarksWithEnergy> makeProposal(
            Optional<VoxelizedMarksWithEnergy> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (!existing.isPresent()) {
            return Optional.empty();
        }

        ProposerContext propContext = context.proposer();

        // Pick an element from the existing configuration
        int index =
                existing.get()
                        .getMarks()
                        .getMarks()
                        .randomIndex(propContext.getRandomNumberGenerator());

        markExst = existing.get().getMarks().getMarks().get(index);

        // We copy the particular mark in question
        markNew = markExst.duplicate();

        VoxelizedMarkMemo pmmMarkNew = propContext.create(markNew);

        // Change the location of the mark
        boolean succ;
        try {
            succ = markProposer.propose(pmmMarkNew, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose a mark due to abnormal exception", e);
        }

        if (!succ) {
            return Optional.empty();
        }

        // We calculate a new Energy by exchanging our marks
        VoxelizedMarksWithEnergy newEnergy = existing.get().shallowCopy();
        try {
            newEnergy.exchange(index, pmmMarkNew, propContext.getEnergyStack());
        } catch (NamedFeatureCalculateException e) {
            throw new KernelCalculateEnergyException(
                    String.format("Cannot exchange index %d", index), e);
        }

        return Optional.of(newEnergy);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return markProposer.isCompatibleWith(testMark);
    }
}
