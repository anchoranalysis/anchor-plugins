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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.mark.MarkWithIdentifierFactory;
import org.anchoranalysis.mpp.bean.proposer.MarkCollectionProposer;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.segment.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculationContext;
import org.anchoranalysis.plugin.mpp.segment.optimization.VoxelizedMarksWithEnergyFactory;

@NoArgsConstructor
public class KernelInitialMarksVoxelized extends KernelIndependent<VoxelizedMarksWithEnergy> {

    // START BEAN LIST
    @BeanField @Getter @Setter private MarkCollectionProposer marksProposer;
    // END BEAN LIST

    private Optional<MarkCollection> lastMarks = Optional.empty();

    public KernelInitialMarksVoxelized(MarkCollectionProposer marksProposer) {
        this.marksProposer = marksProposer;
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return marksProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<VoxelizedMarksWithEnergy> makeProposal(
            Optional<VoxelizedMarksWithEnergy> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        ProposerContext propContext = context.proposer();

        // We don't expect an existing exsting marks, but rather null (or whatever)

        // Initial marks
        Optional<MarkCollection> marksOptional =
                proposeMarks(context.getMarkFactory(), propContext);

        this.lastMarks = marksOptional;

        try {
            return OptionalUtilities.map(
                    marksOptional,
                    marks ->
                            VoxelizedMarksWithEnergyFactory.createFromMarks(
                                    marks, context, getLogger()));
        } catch (CreateException e) {
            throw new KernelCalculateEnergyException("Cannot create voxelixed marks", e);
        }
    }

    private Optional<MarkCollection> proposeMarks(
            MarkWithIdentifierFactory markFactory, ProposerContext propContext)
            throws KernelCalculateEnergyException {
        try {
            return marksProposer.propose(markFactory, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalculateEnergyException(
                    "Failed to propose an initial-marks due to an abnormal error", e);
        }
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {
        // We always accept
        return 1;
    }

    @Override
    public String describeLast() {
        return String.format(
                "initialMarks(size=%d)", this.lastMarks.map(MarkCollection::size).orElse(-1));
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            VoxelizedMarksWithEnergy exst,
            VoxelizedMarksWithEnergy accptd)
            throws UpdateMarkSetException {
        accptd.addAllToUpdatablePairList(updatableMarkSetCollection);
    }

    @Override
    public int[] changedMarkIDArray() {
        return this.lastMarks.map(MarkCollection::createIdArr).orElse(new int[] {});
    }
}
